package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.entity.Invoice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class InvoiceMapperTest {

    @InjectMocks
    private InvoiceMapperImpl invoiceMapper;

    @Test
    void toEntity_shouldMapDtoToEntityCorrectly() {
        // Given
        InvoiceDTO dto = InvoiceDTO.builder()
                .billId("BILL123")
                .accountId("ACC456")
                .issueDate(LocalDate.of(2023, 6, 15))
                .billPeriodFrom(LocalDate.of(2023, 6, 1))
                .billPeriodTo(LocalDate.of(2023, 6, 30))
                .name("Test Invoice")
                .grossAmount(new BigDecimal("100.00"))
                .netAmount(new BigDecimal("80.00"))
                .taxAmount(new BigDecimal("20.00"))
                .rawLine("RAW_LINE_CONTENT")
                .build();

        // When
        Invoice entity = invoiceMapper.mapDtoToEntity(dto);

        // Then
        assertNull(entity.getId()); // ID should be ignored
        assertEquals("BILL123", entity.getBillId());
        assertEquals("ACC456", entity.getAccountId());
        assertEquals(LocalDate.of(2023, 6, 15), entity.getIssueDate());
        assertEquals(LocalDate.of(2023, 6, 1), entity.getBillPeriodFrom());
        assertEquals(LocalDate.of(2023, 6, 30), entity.getBillPeriodTo());
        assertEquals("Test Invoice", entity.getName());
        assertEquals(new BigDecimal("100.00"), entity.getGrossAmount());
        assertEquals(new BigDecimal("80.00"), entity.getNetAmount());
        assertEquals(new BigDecimal("20.00"), entity.getTaxAmount());
    }


    @Test
    void mapDtosToEntities_shouldMapListOfDtosToEntitiesWithCreatedAt() {
        // Given
        InvoiceDTO dto1 = InvoiceDTO.builder()
                .billId("BILL1")
                .accountId("ACC1")
                .name("Invoice 1")
                .grossAmount(new BigDecimal("100.00"))
                .build();

        InvoiceDTO dto2 = InvoiceDTO.builder()
                .billId("BILL2")
                .accountId("ACC2")
                .name("Invoice 2")
                .grossAmount(new BigDecimal("200.00"))
                .build();

        List<InvoiceDTO> dtos = Arrays.asList(dto1, dto2);

        // When
        List<Invoice> entities = invoiceMapper.mapDtosToEntities(dtos);

        // Then
        assertEquals(2, entities.size());
        assertEquals("BILL1", entities.get(0).getBillId());
        assertEquals("Invoice 1", entities.get(0).getName());
        assertEquals(new BigDecimal("100.00"), entities.get(0).getGrossAmount());
        assertEquals("BILL2", entities.get(1).getBillId());
        assertEquals("Invoice 2", entities.get(1).getName());
        assertEquals(new BigDecimal("200.00"), entities.get(1).getGrossAmount());
        assertNotNull(entities.get(0).getCreatedAt());
        assertNotNull(entities.get(1).getCreatedAt());
    }

    @Test
    void mapEntities_shouldHandleNullInput(){
        assertNull(invoiceMapper.mapDtoToEntity(null),"Expected result to be null when input invoiceDTO is null");
        assertNull(invoiceMapper.mapDtosToEntities(null),"Expected result to be null when input invoiceDTOs is null");

    }
}
