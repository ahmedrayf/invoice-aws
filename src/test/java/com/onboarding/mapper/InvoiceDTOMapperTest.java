package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceDTOMapperTest {

    @InjectMocks
    private InvoiceDTOMapperImpl invoiceDTOMapper;

    @Test
    void mapToDto_shouldMapEntityToDtoCorrectly() {
        // Given
        Invoice entity = Invoice.builder()
                .id("INV123")
                .billId("BILL123")
                .accountId("ACC456")
                .issueDate(LocalDate.of(2023, 6, 15))
                .billPeriodFrom(LocalDate.of(2023, 6, 1))
                .billPeriodTo(LocalDate.of(2023, 6, 30))
                .name("Test Invoice")
                .grossAmount(new BigDecimal("100.00"))
                .netAmount(new BigDecimal("80.00"))
                .taxAmount(new BigDecimal("20.00"))
                .createdAt(LocalDateTime.now())
                .build();

        // When
        InvoiceDTO dto = invoiceDTOMapper.mapEntityToDto(entity);

        // Then
        assertEquals("BILL123", dto.getBillId());
        assertEquals("ACC456", dto.getAccountId());
        assertEquals(LocalDate.of(2023, 6, 15), dto.getIssueDate());
        assertEquals(LocalDate.of(2023, 6, 1), dto.getBillPeriodFrom());
        assertEquals(LocalDate.of(2023, 6, 30), dto.getBillPeriodTo());
        assertEquals("Test Invoice", dto.getName());
        assertEquals(new BigDecimal("100.00"), dto.getGrossAmount());
        assertEquals(new BigDecimal("80.00"), dto.getNetAmount());
        assertEquals(new BigDecimal("20.00"), dto.getTaxAmount());
        assertNull(dto.getRawLine()); // rawLine not set from entity
    }

    @Test
    void mapToPageableDto_shouldMapPageOfEntitiesToPageOfDtos() {
        // Given
        Invoice entity1 = Invoice.builder()
                .id("INV1")
                .billId("BILL1")
                .accountId("ACC1")
                .name("Invoice 1")
                .grossAmount(new BigDecimal("50.00"))
                .build();

        Invoice entity2 = Invoice.builder()
                .id("INV2")
                .billId("BILL2")
                .accountId("ACC2")
                .name("Invoice 2")
                .grossAmount(new BigDecimal("75.00"))
                .build();

        Page<Invoice> page = new PageImpl<>(Arrays.asList(entity1, entity2), PageRequest.of(0, 10), 2);

        // When
        Page<InvoiceDTO> result = invoiceDTOMapper.mapToPageDto(page);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals("BILL1", result.getContent().get(0).getBillId());
        assertEquals("Invoice 1", result.getContent().get(0).getName());
        assertEquals(new BigDecimal("50.00"), result.getContent().get(0).getGrossAmount());
        assertEquals("BILL2", result.getContent().get(1).getBillId());
        assertEquals("Invoice 2", result.getContent().get(1).getName());
        assertEquals(new BigDecimal("75.00"), result.getContent().get(1).getGrossAmount());
    }
    @Test
    void  mapToDto_shouldHandleNullEntity(){
        assertNull(invoiceDTOMapper.mapEntityToDto(null),"Expected result to be null when input invoice is null");
    }

}
