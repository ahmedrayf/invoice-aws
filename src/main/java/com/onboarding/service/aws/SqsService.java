package com.onboarding.service.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.dto.SQSMessage;
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
        try {
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(objectMapper.writeValueAsString(message))
                    .build();
                    sqsAsyncClient.sendMessage(sendMsgRequest)
                            .thenAccept(res -> log.info("Message sent successfully. MessageId: {}", res.messageId()));
        }
        catch (SqsException e) {
            log.error("Failed to send invoice to SQS. Message: {}", message, e);
        }catch (JsonProcessingException e) {
            log.error("Error parsing invoice: {}", message, e);
        }
    }
}



