package com.onboarding.integration;

import com.onboarding.config.S3TestConfig;
import com.onboarding.config.SQSMockConfig;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.repo.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("junit")
@Import({S3TestConfig.class, SQSMockConfig.class})
class InvoiceControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InvoiceMapper invoiceMapper;
    @Autowired
    private InvoiceRepository invoiceRepository;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.bucket-name", () -> "test-bucket");
    }


    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
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
