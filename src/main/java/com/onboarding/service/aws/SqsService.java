package com.onboarding.service.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.dto.SQSMessage;
import exception.InvoiceProcessingException;
import exception.MessageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class SqsService {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue.url}")
    private String queueUrl;

    public void sendInvoice(SQSMessage message) {
        log.info("Sending message to SQS");
        try {
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(objectMapper.writeValueAsString(message))
                    .build();
            sqsAsyncClient.sendMessage(sendMsgRequest)
                    .thenAccept(res -> log.info("Message sent successfully. MessageId: {}", res.messageId()))
                    .exceptionally(ex -> {
                        log.error("Failed to send SQS message asynchronously: message: {}",message, ex);
                        throw new InvoiceProcessingException("Failed to send SQS message", ex);
                    });
        } catch (JsonProcessingException e) {
            log.error("Error parsing invoice: {}", message, e);
            throw new MessageProcessingException("Failed to serialize message", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending message onSQS : Message: {}", message, e);
            throw new MessageProcessingException("Unexpected error while sending message onSQS", e);
        }
    }

}



