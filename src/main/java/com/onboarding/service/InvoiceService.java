package com.onboarding.service;

import com.mongodb.MongoException;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.ProcessResult;
import com.onboarding.dto.SQSMessage;
import com.onboarding.entity.Invoice;
import com.onboarding.handler.InvoiceProcessingException;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.service.aws.S3Service;
import com.onboarding.component.CSVParser;
import com.onboarding.service.aws.SqsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private final SqsService sqsService;


    @Value("${processing.batch.size}")
    private int batchSize;


    public Page<InvoiceDTO> getByAccountId(String accountId , int pageNumber , int pageCount){
        return mongoService.getByAccountId(accountId ,pageNumber , pageCount);
    }


    @Async
    public CompletableFuture<ProcessResult> processFileAsync(String invoiceName) {
        log.info("Processing invoice {}", invoiceName);
        ProcessResult result = new ProcessResult(invoiceName);


        try (InputStream inputStream = s3Service.getFileInputStream(invoiceName)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            List<InvoiceDTO> batch = new ArrayList<>(batchSize);
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                parseS3Line(line, lineNumber, batch, result);
                if (batch.size() >= batchSize) {
                    persistInvoices(batch, result);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                persistInvoices(batch, result);
            }


        } catch (Exception e) {
            result.addError(0, "File processing error: " + e.getMessage());
            log.error("File processing failed", e);
        }

        return CompletableFuture.completedFuture(result);
    }

    private void parseS3Line(String line, int lineNumber, List<InvoiceDTO> batch, ProcessResult result) {
        try {
            InvoiceDTO dto = csvParser.parseLine(line, lineNumber);
            batch.add(dto);

        } catch (InvoiceProcessingException e) {
            result.addError(lineNumber, e.getMessage());
            log.warn("Line {} processing failed: {}", lineNumber, e.getMessage());
        }
    }


    private void persistInvoices(List<InvoiceDTO> dtos, ProcessResult result) {

        try {
            List<Invoice> entities = invoiceMapper.mapDtosToEntities(dtos);
            mongoService.saveAll(entities);
            List<SQSMessage> messages = invoiceMapper.mapToSqs(dtos);
            messages.forEach(sqsService::sendInvoice);

            result.incrementSuccessCount(dtos.size());
        } catch (MongoException e) {
            log.error("Batch save failed", e);
            result.addError(-1, "Batch save error: " + e.getMessage());
            throw e;
        }
    }




}

