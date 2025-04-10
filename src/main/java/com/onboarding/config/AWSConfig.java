package com.onboarding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AWSConfig {

    AwsBasicCredentials basicCredentials;
    @Value("${aws.region}")
    private String region;

    public AWSConfig(@Value("${aws.access.key}")
                     String accessKey,
                     @Value("${aws.access.secret-key}")
                     String secretKey
    ) {
        this.basicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
    }


    @Bean
    SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .forcePathStyle(true)
                .build();
    }
}
