package com.onboarding.service;

import com.mongodb.MongoException;
import com.onboarding.component.CSVParser;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.ProcessResult;
import com.onboarding.dto.SQSMessage;
import com.onboarding.entity.Invoice;
import com.onboarding.exception.InvoiceProcessingException;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.mapper.SQSMessageMapper;
import com.onboarding.service.aws.S3Service;
import com.onboarding.service.aws.SqsService;
import com.onboarding.exception.MessageProcessingException;
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
    private SQSMessageMapper sqsMessageMapper;
    @Mock
    private SqsService sqsService;
    @InjectMocks
    private InvoiceService invoiceService;
    private static final String TEST_FILE_NAME = "invoice_20250301.csv";
    private static final String FILE_PATH = "src/test/resources/invoices/success/csv/invoice_20250301.csv";
    private static final int BATCH_SIZE = 1;

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

        InvoiceDTO dto1 = new InvoiceDTO();
        InvoiceDTO dto2 = new InvoiceDTO();
        when(csvParser.parseLine(lines.get(0), 1)).thenReturn(dto1);
        when(csvParser.parseLine(lines.get(1), 2)).thenReturn(dto2);

        when(invoiceMapper.mapDtosToEntities(anyList()))
                .thenReturn(List.of(new Invoice()));
        when(sqsMessageMapper.mapDtosToSqsMessages(anyList()))
                .thenReturn(List.of(new SQSMessage()));

        // When
        CompletableFuture<ProcessResult> future = invoiceService.processFileAsync(TEST_FILE_NAME);
        ProcessResult result = future.get();

        // Then
        assertEquals(2, result.getSuccessCount());
        verify(mongoService , times(2)).saveAll(anyList());
        verify(sqsService, times(2)).sendInvoice(any());
    }

    @Test
    void processFileAsync_shouldProcessMultipleBatches() throws Exception {
        // Given
        File file = ResourceUtils.getFile(FILE_PATH);
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

        // Create input with 3 lines (2 in first batch, 1 in second)

        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));

        when(csvParser.parseLine(lines.get(0), 1)).thenReturn(new InvoiceDTO());
        when(csvParser.parseLine(lines.get(1), 2)).thenReturn(new InvoiceDTO());

        when(invoiceMapper.mapDtosToEntities(anyList())).thenReturn(List.of(new Invoice()));
        when(sqsMessageMapper.mapDtosToSqsMessages(anyList())).thenReturn(List.of(new SQSMessage()));

        // When
        CompletableFuture<ProcessResult> future = invoiceService.processFileAsync(TEST_FILE_NAME);
        ProcessResult result = future.get();

        // Then
        assertEquals(2, result.getSuccessCount());
        verify(mongoService, times(2)).saveAll(anyList()); // Called twice (batch of 2 and batch of 1)
        verify(sqsService, times(2)).sendInvoice(any()); // Called for each record
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

        when(invoiceMapper.mapDtosToEntities(anyList()))
                .thenReturn(List.of(new Invoice()));
        when(sqsMessageMapper.mapDtosToSqsMessages(anyList()))
                .thenReturn(List.of(new SQSMessage()));

        doNothing().when(mongoService).saveAll(anyList());
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
    void processFileAsync_shouldHandleS3Exception() throws Exception {
        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenThrow(new RuntimeException("S3 access failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> invoiceService.processFileAsync(TEST_FILE_NAME));
        assertEquals("S3 access failed", ex.getMessage());

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
    void processFileAsync_shouldThrowInvoiceProcessingException_whenIOExceptionOccurs() throws Exception {
        // Given
        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenThrow(new IOException("Stream read error"));

        // When & Then
        InvoiceProcessingException ex = assertThrows(InvoiceProcessingException.class,
                () -> invoiceService.processFileAsync(TEST_FILE_NAME));

        assertTrue(ex.getMessage().contains("Error while reading input stream"));
    }

    @Test
    void processFileAsync_shouldHandleInvoiceProcessingException_whenSavingToDB() throws Exception {
        // Given
        File file = ResourceUtils.getFile(FILE_PATH);
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));
        when(csvParser.parseLine(lines.get(0), 1)).thenReturn(new InvoiceDTO());

        when(invoiceMapper.mapDtosToEntities(anyList()))
                .thenReturn(List.of(new Invoice()));

        doThrow(new InvoiceProcessingException("DB Save error"))
                .when(mongoService).saveAll(anyList());

        // When & Then
        InvoiceProcessingException ex = assertThrows(InvoiceProcessingException.class,
                () -> invoiceService.processFileAsync(TEST_FILE_NAME));

        assertTrue(ex.getMessage().contains("DB Save error"));
    }

    @Test
    void processFileAsync_shouldHandleMongoException_whenSavingToDB() throws Exception {
        // Given
        File file = ResourceUtils.getFile(FILE_PATH);
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));
        when(csvParser.parseLine(lines.get(0), 1)).thenReturn(new InvoiceDTO());

        when(invoiceMapper.mapDtosToEntities(anyList()))
                .thenReturn(List.of(new Invoice()));

        doThrow(new MongoException("Mongo Error"))
                .when(mongoService).saveAll(anyList());

        // When & Then
        MongoException ex = assertThrows(MongoException.class,
                () -> invoiceService.processFileAsync(TEST_FILE_NAME));

        assertTrue(ex.getMessage().contains("Mongo Error"));
    }
    @Test
    void processFileAsync_shouldHandleMessageProcessingException_whenSendingToSQS() throws Exception {
        // Given
        File file = ResourceUtils.getFile(FILE_PATH);
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

        when(s3Service.getFileInputStream(TEST_FILE_NAME))
                .thenReturn(Files.newInputStream(file.toPath()));
        when(csvParser.parseLine(lines.get(0), 1)).thenReturn(new InvoiceDTO());

        when(invoiceMapper.mapDtosToEntities(anyList()))
                .thenReturn(List.of(new Invoice()));
        when(sqsMessageMapper.mapDtosToSqsMessages(anyList()))
                .thenReturn(List.of(new SQSMessage()));

        doNothing().when(mongoService).saveAll(anyList());
        doThrow(new MessageProcessingException("SQS Send Error"))
                .when(sqsService).sendInvoice(any(SQSMessage.class));

        // When & Then
        MessageProcessingException ex = assertThrows(MessageProcessingException.class,
                ()-> invoiceService.processFileAsync(TEST_FILE_NAME));

        assertTrue(ex.getMessage().contains("SQS Send Error"));
    }






}


