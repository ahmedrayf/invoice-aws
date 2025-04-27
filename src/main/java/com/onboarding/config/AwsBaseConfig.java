package com.onboarding.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

@Configuration
public class AwsBaseConfig {
    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.access.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    protected String region;

    protected AwsBasicCredentials basicCredentials;

    @PostConstruct
    public void init() {
        this.basicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
    }
}
