package com.onboarding.config;

import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@TestConfiguration
@ActiveProfiles("junit")
public class S3TestConfig {

	@Value("${aws.s3.mock.port}")
	private int s3MockPort;
	@Value("${aws.region}")
	private String region;
	@Value("${aws.access.key}")
	private  String accessKey = "test-access-key";
	@Value("${aws.access.secret-key}")
	private  String secretKey = "test-secret-key";

	@Bean(initMethod = "start", destroyMethod = "stop")
	public S3Mock s3MockServer() {
		return new S3Mock.Builder()
				.withPort(s3MockPort)
				.withInMemoryBackend()
				.build();
	}

	@Primary
	@Bean
	public S3Client testS3Client() {
		return S3Client.builder()
				.endpointOverride(URI.create("http://localhost:" + s3MockPort))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKey, secretKey)))
				.region(Region.of(region))
				.serviceConfiguration(b -> b.pathStyleAccessEnabled(true))
				.build();
	}



}
