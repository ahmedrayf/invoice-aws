package controller;

import com.onboarding.controller.InvoiceController;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.ProcessResult;
import com.onboarding.dto.response.ApiResponse;
import com.onboarding.dto.response.PageableResponse;
import com.onboarding.service.InvoiceService;
import com.onboarding.service.MongoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("junit")
class InvoiceControllerTest {

    @Mock
    private MongoService mongoService;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    private InvoiceDTO sampleInvoice;
    private Page<InvoiceDTO> invoicePage;

    @BeforeEach
    void setUp() {
        sampleInvoice = InvoiceDTO.builder()
                .billId("BILL001")
                .accountId("ACC001")
                .issueDate(LocalDate.now())
                .billPeriodFrom(LocalDate.now().minusDays(30))
                .billPeriodTo(LocalDate.now())
                .name("Test Invoice")
                .grossAmount(new BigDecimal("100.00"))
                .netAmount(new BigDecimal("80.00"))
                .taxAmount(new BigDecimal("20.00"))
                .rawLine("raw line data")
                .build();

        List<InvoiceDTO> invoices = Collections.singletonList(sampleInvoice);
        invoicePage = new PageImpl<>(invoices, PageRequest.of(0, 10), 1);
    }

    @Test
    void getInvoicesByAccountId_shouldReturnPageableResponse() {
        // Arrange
        when(mongoService.getByAccountId(anyString(), anyInt(), anyInt()))
                .thenReturn(invoicePage);

        // Act
        ResponseEntity<PageableResponse<List<InvoiceDTO>>> response =
                invoiceController.getInvoicesByAccountId("ACC001", 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        PageableResponse<List<InvoiceDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals("Success", body.getMessage());
        assertEquals(1, body.getBody().size());
        assertEquals(sampleInvoice, body.getBody().get(0));
        assertEquals(0, body.getCurrentPage());
        assertEquals(1, body.getTotalItems());
        assertEquals(1, body.getTotalPages());
        assertEquals(1, body.getCurrentItems());
    }

    @Test
    void getInvoicesByAccountId_shouldReturnNotFoundWhenEmpty() {
        // Arrange
        Page<InvoiceDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(mongoService.getByAccountId(anyString(), anyInt(), anyInt()))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<PageableResponse<List<InvoiceDTO>>> response =
                invoiceController.getInvoicesByAccountId("NON_EXISTENT", 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        PageableResponse<List<InvoiceDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals("Not Found", body.getMessage());
        assertTrue(body.getBody().isEmpty());
    }

    @Test
    void processInvoiceFile_shouldProcessSuccessfully() throws ExecutionException, InterruptedException {
        // Arrange
        String filename = "invoice_20250301.csv";
        ProcessResult processResult = new ProcessResult(filename);
        processResult.incrementSuccessCount(10);

        when(invoiceService.processFileAsync(filename))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(processResult));

        // Act
        ResponseEntity<ApiResponse<String>> response =
                invoiceController.processInvoiceFile(filename);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Success", body.getMessage());
        assertTrue(body.getBody().contains("Success: 10"));
        assertNull(body.getErrors());
    }

    @Test
    void processInvoiceFile_shouldIncludeErrorsWhenPresent() throws ExecutionException, InterruptedException {
        // Arrange
        String filename = "invoice_20250301.csv";
        ProcessResult processResult = new ProcessResult(filename);
        processResult.incrementSuccessCount(8);
        processResult.addError(3, "Invalid amount");
        processResult.addError(7, "Missing field");

        when(invoiceService.processFileAsync(filename))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(processResult));

        // Act
        ResponseEntity<ApiResponse<String>> response =
                invoiceController.processInvoiceFile(filename);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Success", body.getMessage());
        assertTrue(body.getBody().contains("Success: 8"));
        assertTrue(body.getBody().contains("Errors: 2"));
        assertNotNull(body.getErrors());
        assertInstanceOf(Map.class, body.getErrors());
        Map<?, ?> errorsMap = (Map<?, ?>) body.getErrors();
        assertEquals(2, errorsMap.size());
        assertEquals("Invalid amount", errorsMap.get(3));
        assertEquals("Missing field", errorsMap.get(7));
    }


    @Test
    void processInvoiceFile_shouldHandleExecutionException() {
        // Arrange
        String filename = "invoice_20250301.csv";
        when(invoiceService.processFileAsync(filename))
                .thenAnswer(invocation -> {
                    throw new ExecutionException("Processing failed", new RuntimeException("Error"));
                });

        // Act & Assert
        ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> invoiceController.processInvoiceFile(filename)
        );
        assertEquals("Processing failed", exception.getMessage());
    }

}
