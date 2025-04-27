package com.onboarding.service;

import com.mongodb.MongoException;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.ProcessResult;
import com.onboarding.dto.SQSMessage;
import com.onboarding.entity.Invoice;
import exception.InvoiceProcessingException;
import exception.MessageProcessingException;
import exception.ResourceNotFoundException;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.mapper.SQSMessageMapper;
import com.onboarding.service.aws.S3Service;
import com.onboarding.component.CSVParser;
import com.onboarding.service.aws.SqsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    private final S3Service s3Service;
    private final CSVParser csvParser;
    private final MongoService mongoService;
    private final InvoiceMapper invoiceMapper;
    private final SQSMessageMapper sqsMessageMapper;
    private final SqsService sqsService;


    @Value("${processing.batch.size}")
    private int batchSize;

    public Page<InvoiceDTO> getInvoicesByAccountId(String accountId, int pageNumber, int pageCount) {
        return mongoService.getInvoicesByAccountId(accountId, pageNumber, pageCount);
    }

    @Async
    public CompletableFuture<ProcessResult> processFileAsync(String invoiceName) {

        ProcessResult result = ProcessResult.builder().filename(invoiceName).build();
        log.info("Processing invoice {}", invoiceName);

        try (InputStream inputStream = s3Service.getFileInputStream(invoiceName)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            List<InvoiceDTO> batch = new ArrayList<>(batchSize);
            log.info("batches {}", batchSize);
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                parseInvoiceLine(line, lineNumber, batch, result);
                if (batch.size() >= batchSize) {
                    saveInvoicesToDB(batch, result);
                    sendMessages(batch, result);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                saveInvoicesToDB(batch, result);
                sendMessages(batch, result);
            }

        } catch (ResourceNotFoundException e) {
            log.error("File not found in S3: {}", invoiceName, e);
            throw new ResourceNotFoundException("File not found in S3: " + invoiceName, e);
        } catch (IOException e) {
            log.error("Error while reading input stream: {}", invoiceName, e);
            throw new InvoiceProcessingException("Error while reading input stream", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    private void parseInvoiceLine(String line, int lineNumber, List<InvoiceDTO> batch, ProcessResult result) {
        try {
            InvoiceDTO dto = csvParser.parseLine(line, lineNumber);
            batch.add(dto);

        } catch (InvoiceProcessingException e) {
            result.addError(lineNumber, e.getMessage());
        }
    }


    private void saveInvoicesToDB(List<InvoiceDTO> dtos, ProcessResult result) {
        log.info("Persisting {} invoices", dtos.size());
        try {
            List<Invoice> entities = invoiceMapper.mapDtosToEntities(dtos);
            mongoService.saveAll(entities);
        } catch (InvoiceProcessingException e) {
            String errorMsg = e.getMessage();
            log.error("Batch save failed: {}", errorMsg, e);
            result.addError(0, errorMsg);
            throw e;
        } catch (MongoException e) {
            log.error("Unexpected error during batch save", e);
            result.addError(0, "Unexpected error during batch processing");
            throw e;
        }
    }

    private void sendMessages(List<InvoiceDTO> dtos, ProcessResult result) {
        List<SQSMessage> messages = sqsMessageMapper.mapDtosToSqsMessages(dtos);
        for (SQSMessage message : messages) {
            try {
                sqsService.sendInvoice(message);
                result.incrementSuccessCount(1);
            } catch (MessageProcessingException e) {
                result.addError(0, "Failed to send message to SQS for message: " + message.getContent());
                throw e;
            }
        }

    }
}

