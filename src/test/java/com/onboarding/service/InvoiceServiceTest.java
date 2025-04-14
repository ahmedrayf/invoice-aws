package com.onboarding.service;

import com.onboarding.component.CSVParser;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.ProcessResult;
import com.onboarding.dto.SQSMessage;
import com.onboarding.entity.Invoice;
import com.onboarding.handler.InvoiceProcessingException;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.service.aws.S3Service;
import com.onboarding.service.aws.SqsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private S3Service s3Service;
    @Mock
    private CSVParser csvParser;
    @Mock
    private MongoService mongoService;
    @Mock
    private InvoiceMapper invoiceMapper;
    @Mock
    private SqsService sqsService;
    @InjectMocks
    private InvoiceService invoiceService;
    private static final String TEST_FILE_NAME = "invoice_20250301.csv";
    private static final String FILE_PATH = "src/test/resources/invoices/success/csv/invoice_20250301.csv";
    private static final int BATCH_SIZE = 100;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invoiceService, "batchSize", BATCH_SIZE);
    }

    @Test
    void processFileAsync_shouldProcessRealCSVFile() throws Exception {
        // Given
        File file = ResourceUtils.getFile(FILE_PATH);
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));
        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));

        InvoiceDTO dto1 = new InvoiceDTO();
        InvoiceDTO dto2 = new InvoiceDTO();
        when(csvParser.parseLine(lines.get(0), 1)).thenReturn(dto1);
        when(csvParser.parseLine(lines.get(1), 2)).thenReturn(dto2);

        when(invoiceMapper.mapDtosToEntities(anyList()))
                .thenReturn(List.of(new Invoice(), new Invoice()));
        when(invoiceMapper.mapToSqs(anyList()))
                .thenReturn(List.of(new SQSMessage(), new SQSMessage()));

        // When
        CompletableFuture<ProcessResult> future = invoiceService.processFileAsync(TEST_FILE_NAME);
        ProcessResult result = future.get();

        // Then
        assertEquals(2, result.getSuccessCount());
        verify(mongoService).saveAll(anyList());
        verify(sqsService, times(2)).sendInvoice(any());
    }


    @Test
    void processFileAsync_shouldHandleMixedValidAndInvalidLines() throws Exception {
        File file = ResourceUtils.getFile(FILE_PATH);
        // Given
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));

        when(csvParser.parseLine(lines.get(0), 1)).thenReturn(new InvoiceDTO());
        when(csvParser.parseLine(lines.get(1), 2))
                .thenThrow(new InvoiceProcessingException("Invalid format"));

        // When
        CompletableFuture<ProcessResult> future = invoiceService.processFileAsync(TEST_FILE_NAME);
        ProcessResult result = future.get();

        // Then
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void processFileAsync_shouldHandleInvalidLines() throws Exception {
        File file = ResourceUtils.getFile(FILE_PATH);
        // Given
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));

        when(csvParser.parseLine(lines.get(0), 1))
                .thenThrow(new InvoiceProcessingException("Invalid format"));
        when(csvParser.parseLine(lines.get(1), 2))
                .thenThrow(new InvoiceProcessingException("Invalid format"));

        // When
        CompletableFuture<ProcessResult> future = invoiceService.processFileAsync(TEST_FILE_NAME);
        ProcessResult result = future.get();

        // Then
        assertEquals(0, result.getSuccessCount());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void processFileAsync_shouldHandleS3Errors() throws Exception {
        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenThrow(new RuntimeException("S3 error"));

        ProcessResult result = invoiceService.processFileAsync(TEST_FILE_NAME).get();

        assertEquals(0, result.getSuccessCount());
        assertTrue(result.getErrors().get(0).contains("S3 error"));
    }

    @Test
    void processFileAsync_shouldHandleEmptyFile() throws Exception {

        InputStream inputStream = new ByteArrayInputStream(
                String.join("\n", List.of()).getBytes());
        when(s3Service.getFileInputStream(TEST_FILE_NAME)).thenReturn(inputStream);

        ProcessResult result = invoiceService.processFileAsync(TEST_FILE_NAME).get();

        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void shouldRejectNonCsvFiles() throws Exception {
        ProcessResult result = invoiceService.processFileAsync("invoice.pdf").get();
        assertEquals(0, result.getSuccessCount());
        assertTrue(result.getErrors().get(0).contains(".csv files are allowed"));
    }


}
