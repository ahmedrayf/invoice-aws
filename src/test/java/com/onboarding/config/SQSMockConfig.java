package com.onboarding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("junit")
public class SQSMockConfig {

	@Value("${aws.sqs.queue.url}")
	private String queueUrl;

	@Bean
	@Primary
	SqsAsyncClient sqsAsyncClient() {
		SqsAsyncClient client = mock(SqsAsyncClient.class);

		// Mock queue URL response
		when(client.getQueueUrl(any(GetQueueUrlRequest.class)))
				.thenReturn(completedFuture(
						GetQueueUrlResponse.builder()
								.queueUrl(queueUrl)
								.build()));

		// Mock send message response
		when(client.sendMessage(any(SendMessageRequest.class)))
				.thenReturn(completedFuture(
						SendMessageResponse.builder()
								.messageId("mock-message-id")
								.build()));

		return client;
	}
}