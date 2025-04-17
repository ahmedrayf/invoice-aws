package com.onboarding.integration;

import com.onboarding.config.S3TestConfig;
import com.onboarding.config.SQSMockConfig;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.repo.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("junit")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({S3TestConfig.class, SQSMockConfig.class})
class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private S3Client s3Client;
    @Autowired
    private InvoiceMapper invoiceMapper;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Value("${aws.s3.bucket-name}")
    private String testBucketName;


    @BeforeAll
    void createBucket(){
        CreateBucketRequest createBucketRequest1 = CreateBucketRequest.builder().bucket(testBucketName).build();
        s3Client.createBucket(createBucketRequest1);
    }


    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
    }

    @Test
    void verifyS3Connection() {
        // Verify bucket exists
        List<Bucket> buckets = s3Client.listBuckets().buckets();
        assertFalse(buckets.isEmpty());
        assertEquals(testBucketName, buckets.get(0).name());

        // Verify endpoint configuration
        String endpoint = s3Client.serviceClientConfiguration().endpointOverride()
                .orElse(URI.create("default")).toString();
        assertTrue(endpoint.matches("http://localhost:\\d+"));
    }

    @Test
    void processInvoiceFile_shouldProcessValidFiles() throws Exception {

        String fileName = "invoice_20250301.csv";

        File file = ResourceUtils.getFile("src/test/resources/invoices/success/csv/invoice_20250301.csv");
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        log.info("File content before upload: \n{}", content);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(testBucketName)
                        .key(fileName)
                        .build(),
                RequestBody.fromBytes(content.getBytes(StandardCharsets.UTF_8))
        );

        mockMvc.perform(post("/v1/invoice/{invoiceName}", fileName))

                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").value("Success"),
                        jsonPath("$.body").value(containsString("Success: 2"))
                );

        await().atMost(5, SECONDS).until(() -> !invoiceRepository.findAll().isEmpty());

        assertFalse(invoiceRepository.findAll().isEmpty());
    }


    @Test
    void getInvoicesByAccountId_shouldReturnInvoices() throws Exception {
        // Given
        String accountId = "ACC001";
        InvoiceDTO invoice = createTestInvoice(accountId);
        invoiceRepository.save(invoiceMapper.toEntity(invoice));

        // When & Then
        mockMvc.perform(get("/v1/invoice/findByAccountId/{accountId}", accountId)
                        .param("pageNum", "0")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.body[0].accountId", is(accountId)))
                .andExpect(jsonPath("$.totalItems", is(1)));
    }

    @Test
    void getInvoicesByAccountId_shouldReturnEmptyForUnknownAccount() throws Exception {
        // Given
        String unknownAccountId = "UNKNOWN_ACC";

        // When & Then
        mockMvc.perform(get("/v1/invoice/findByAccountId/{accountId}", unknownAccountId)
                        .param("pageNum", "0")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Not Found")));
    }


    @Test
    void processInvoiceFile_shouldHandleMissingFile() throws Exception {
        // Given
        String nonExistentFile = "invoice_21220301.csv";

        // When & Then
        mockMvc.perform(post("/v1/invoice/{invoiceName}", nonExistentFile)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", notNullValue()))
                .andExpect(jsonPath("$.body", containsString("Errors: ")));
    }

    @Test
    void processInvoiceFile_shouldRejectInvalidFilename() throws Exception {
        // Given
        String invalidFilename = "invalid_name.txt";

        // When & Then
        mockMvc.perform(post("/v1/invoice/{invoiceName}", invalidFilename)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid file name format")));
    }

    private InvoiceDTO createTestInvoice(String accountId) {
        return InvoiceDTO.builder()
                .billId("BILL-" + System.currentTimeMillis())
                .accountId(accountId)
                .issueDate(LocalDate.now())
                .billPeriodFrom(LocalDate.now().minusDays(30))
                .billPeriodTo(LocalDate.now())
                .name("Test Invoice")
                .grossAmount(new BigDecimal("100.00"))
                .netAmount(new BigDecimal("80.00"))
                .taxAmount(new BigDecimal("20.00"))
                .rawLine("test|data|line")
                .build();
    }
}
