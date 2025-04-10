package com.onboarding.config;

import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;

@TestConfiguration
@ActiveProfiles("junit")
public class S3TestConfig {

	@Value("${aws.region}")
	private String region;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Bean
	@Primary
	S3Client s3Client() throws IOException {

		// Generate random free port
		ServerSocket serverSocket = new ServerSocket(0);
		int port = serverSocket.getLocalPort();
		serverSocket.close();

		S3Mock api = new S3Mock.Builder().withPort(port).withInMemoryBackend().build();
		api.start(); // Start the Mock S3 server locally on available port

		// Create S3Client with v2 SDK
		S3Client s3Client = S3Client.builder().endpointOverride(URI.create("http://localhost:" + port)) // Mock S3
				// endpoint
				.region(Region.of(region)).credentialsProvider(AnonymousCredentialsProvider.create())
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();

		// Create buckets in the mock S3
		CreateBucketRequest createBucketRequest1 = CreateBucketRequest.builder().bucket(bucketName).build();
		s3Client.createBucket(createBucketRequest1);

		return s3Client;
	}

}
