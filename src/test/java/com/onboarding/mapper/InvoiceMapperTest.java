package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.SQSMessage;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        Invoice entity = invoiceMapper.toEntity(dto);

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
        InvoiceDTO dto = invoiceMapper.mapToDto(entity);

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
        Page<InvoiceDTO> result = invoiceMapper.mapToPageableDto(page);

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
    void toSqsMessage_shouldMapInvoiceDtoToSqsMessage() {
        // Given
        InvoiceDTO dto = InvoiceDTO.builder()
                .accountId("ACC456")
                .rawLine("RAW_LINE_CONTENT")
                .issueDate(LocalDate.of(2023, 6, 15))
                .build();

        // When
        SQSMessage message = invoiceMapper.toSqsMessage(dto);

        // Then
        assertThat(LocalDate.of(2023, 6, 15)).isEqualTo( message.getIssueDate());
        assertEquals("RAW_LINE_CONTENT", message.getContent());
        assertEquals("ACC456", message.getAccountId());
    }

    @Test
    void mapToSqs_shouldMapListOfInvoiceDtosToSqsMessagesWithPublishDate() {
        // Given
        InvoiceDTO dto1 = InvoiceDTO.builder()
                .accountId("ACC1")
                .rawLine("RAW_LINE_1")
                .issueDate(LocalDate.of(2023, 6, 15))
                .build();

        InvoiceDTO dto2 = InvoiceDTO.builder()
                .accountId("ACC2")
                .rawLine("RAW_LINE_2")
                .issueDate(LocalDate.of(2024, 6, 15))
                .build();

        List<InvoiceDTO> dtos = Arrays.asList(dto1, dto2);

        // When
        List<SQSMessage> messages = invoiceMapper.mapToSqs(dtos);

        // Then
        assertEquals(2, messages.size());
        assertEquals("ACC1", messages.get(0).getAccountId());
        assertEquals("ACC2", messages.get(1).getAccountId());
        assertEquals("RAW_LINE_1", messages.get(0).getContent());
        assertEquals("RAW_LINE_2", messages.get(1).getContent());
        assertThat(LocalDate.of(2023, 6, 15)).isEqualTo(messages.get(0).getIssueDate());
        assertThat(LocalDate.of(2024, 6, 15)).isEqualTo(messages.get(1).getIssueDate());

    }
}
