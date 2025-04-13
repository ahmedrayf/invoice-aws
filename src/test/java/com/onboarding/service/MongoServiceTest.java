package com.onboarding.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import com.onboarding.mapper.InvoiceMapper;
import com.onboarding.repo.InvoiceRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class MongoServiceTest {

    @Mock
    private InvoiceRepository invoiceRepo;
    @Mock
    private InvoiceMapper invoiceMapper;
    @InjectMocks
    private MongoService mongoService;
    private static List<Invoice> testInvoices;
    private static final PageRequest DEFAULT_PAGE = PageRequest.of(0, 10);

    @BeforeAll
    static void setup() throws IOException {
        File file = ResourceUtils.getFile("src/test/resources/invoices/success/entity/invoices-entities.json");
        String content = Files.readString(file.toPath());
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        testInvoices = mapper.readValue(content, new TypeReference<List<Invoice>>() {});
    }

    @Test
    void saveAll_WithValidInvoices_ShouldSaveSuccessfully() {
        // When
        mongoService.saveAll(testInvoices);

        // Then
        verify(invoiceRepo, times(1)).saveAll(testInvoices);
        verifyNoMoreInteractions(invoiceRepo);
    }

    @Test
    void saveAll_WithNullList_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mongoService.saveAll(null)
        );
        assertEquals("Invoices is null or empty", exception.getMessage());
        verifyNoInteractions(invoiceRepo);
    }

    @Test
    void saveAll_WithEmptyList_ShouldThrowException() {
        List<Invoice> emptyList = Collections.emptyList();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mongoService.saveAll(emptyList)
        );
        assertEquals("Invoices is null or empty", exception.getMessage());
        verifyNoInteractions(invoiceRepo);
    }

    @Test
    void getByAccountId_WithNonExistingAccount_ShouldReturnEmptyPage() {
        // Given
        String nonExistingAccount = "0000000000";
        Page<Invoice> emptyPage = new PageImpl<>(Collections.emptyList(), DEFAULT_PAGE, 0);

        when(invoiceRepo.findByAccountId(nonExistingAccount, DEFAULT_PAGE))
                .thenReturn(emptyPage);
        when(invoiceMapper.mapToPageableDto(emptyPage))
                .thenReturn(new PageImpl<>(Collections.emptyList(), DEFAULT_PAGE, 0));

        // When
        Page<InvoiceDTO> result = mongoService.getByAccountId(nonExistingAccount, 0, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getByAccountId_WithPagination_ShouldUseCorrectPageable() {
        // Given
        int pageNumber = 1;
        int pageSize = 2;
        PageRequest customPage = PageRequest.of(pageNumber, pageSize);
        Invoice testInvoice = testInvoices.get(1);
        Page<Invoice> invoicePage = new PageImpl<>(List.of(testInvoice), customPage, 1);
        InvoiceDTO expectedDto = new InvoiceDTO(); // Add expected mapping

        when(invoiceRepo.findByAccountId(testInvoice.getAccountId(), customPage))
                .thenReturn(invoicePage);
        when(invoiceMapper.mapToPageableDto(invoicePage))
                .thenReturn(new PageImpl<>(List.of(expectedDto), customPage, 1));

        // When
        Page<InvoiceDTO> result = mongoService.getByAccountId(
                testInvoice.getAccountId(), pageNumber, pageSize
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(pageNumber, result.getPageable().getPageNumber());
        assertEquals(pageSize, result.getPageable().getPageSize());

    }

}
