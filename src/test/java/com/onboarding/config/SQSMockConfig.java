package com.onboarding.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("junit")
public class SQSMockConfig {

	@Bean
	@Primary
	SqsAsyncClient sqsAsyncClient() {
		SqsAsyncClient client = mock(SqsAsyncClient.class);
		Mockito.when(client.getQueueUrl(Mockito.any(GetQueueUrlRequest.class))).thenReturn(
				CompletableFuture.completedFuture(GetQueueUrlResponse.builder().queueUrl("mockQueueUrl").build()));
		return client;
	}
}