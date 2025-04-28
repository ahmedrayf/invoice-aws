package com.onboarding.mapper;

import com.onboarding.dto.InvoiceDTO;
import com.onboarding.dto.SQSMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SQSMessageMapperTest {

    @InjectMocks
    private SQSMessageMapperImpl sqsMessageMapper;


    @Test
    void toSqsMessage_shouldMapInvoiceDtoToSqsMessage() {
        // Given
        InvoiceDTO dto = InvoiceDTO.builder()
                .accountId("ACC456")
                .rawLine("RAW_LINE_CONTENT")
                .issueDate(LocalDate.of(2023, 6, 15))
                .build();

        // When
        SQSMessage message = sqsMessageMapper.mapDtoToSqsMessage(dto);

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
        List<SQSMessage> messages = sqsMessageMapper.mapDtosToSqsMessages(dtos);

        // Then
        assertEquals(2, messages.size());
        assertEquals("ACC1", messages.get(0).getAccountId());
        assertEquals("ACC2", messages.get(1).getAccountId());
        assertEquals("RAW_LINE_1", messages.get(0).getContent());
        assertEquals("RAW_LINE_2", messages.get(1).getContent());
        assertThat(LocalDate.of(2023, 6, 15)).isEqualTo(messages.get(0).getIssueDate());
        assertThat(LocalDate.of(2024, 6, 15)).isEqualTo(messages.get(1).getIssueDate());

    }
    @Test
    void toSqsMessages_shouldHandleNullInput(){
        assertNull(sqsMessageMapper.mapDtoToSqsMessage(null),"Expected result to be null when input invoiceDto is null");
        assertNull(sqsMessageMapper.mapDtosToSqsMessages(null),"Expected result to be null when input invoiceDtoS is null");

    }

}
