package com.onboarding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.dto.SQSMessage;
import com.onboarding.service.aws.SqsService;
import exception.MessageProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("junit")
class SqsServiceTest {


    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SqsService sqsService;

    private SQSMessage sampleSqsMessage;

    @BeforeEach
    void setUp() throws Exception {
        Field field = SqsService.class.getDeclaredField("queueUrl");
        field.setAccessible(true);
        field.set(sqsService, "mockQueueUrl");

        sampleSqsMessage = SQSMessage.builder()
                .accountId("acc123")
                .issueDate(LocalDate.of(2024, 1, 1))
                .publishDate(LocalDate.of(2024, 1 , 2))
                .content("Invoice data")
                .build();
    }


    @Test
    void sendInvoice_shouldSendMessageSuccessfully() throws Exception {
        // Arrange

        String serialized = "{\"accountId\":\"acc123\",\"issueDate\":\"2024-01-01\",\"publishDate\":\"2024-01-02\",\"content\":\"Invoice data\"}";

        when(objectMapper.writeValueAsString(sampleSqsMessage)).thenReturn(serialized);
        CompletableFuture<SendMessageResponse> future = new CompletableFuture<>();
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenReturn(future);

        // Act
        sqsService.sendInvoice(sampleSqsMessage);

        // Assert
        SendMessageResponse mockResponse = SendMessageResponse.builder().messageId("test-message-id-999").build();
        future.complete(mockResponse);

        // Assert
        assertThat(future.isDone()).isTrue();
        assertThat(future.get().messageId()).isEqualTo("test-message-id-999");
    }

    @Test
    void sendInvoice_shouldHandleJsonProcessingException() throws Exception {
        when(objectMapper.writeValueAsString(sampleSqsMessage))
                .thenThrow(new JsonProcessingException("JSON fail") {});

        // Act & Assert
        assertThrows(MessageProcessingException.class, () ->
            sqsService.sendInvoice(sampleSqsMessage));

        // Verify
        verify(objectMapper).writeValueAsString(sampleSqsMessage);
        verifyNoInteractions(sqsAsyncClient);
    }

    @Test
    void sendInvoice_shouldHandleSQSException() throws Exception {
        // When
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("serialized");
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class))).thenThrow(SqsException.class);

        // Act & Assert
        assertThrows(MessageProcessingException.class, () ->
                sqsService.sendInvoice(sampleSqsMessage));
    }

}
