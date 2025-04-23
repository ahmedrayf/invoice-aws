package com.onboarding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("!junit")
public class S3Config {

    AwsBasicCredentials basicCredentials;
    @Value("${aws.region}")
    private String region;

    public S3Config(@Value("${aws.access.key}")
                     String accessKey,
                     @Value("${aws.access.secret-key}")
                     String secretKey
    ) {
        this.basicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
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
