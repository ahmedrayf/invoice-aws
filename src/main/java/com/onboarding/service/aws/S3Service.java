package com.onboarding.service.aws;

import com.onboarding.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public InputStream getFileInputStream(String key) throws IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(request);

        } catch (S3Exception e) {
            log.error("Failed to get file from S3: {}/{} - {}", bucketName, key, e.getMessage());
            throw new ResourceNotFoundException("S3 access failed: " + e.getMessage(), e);
        }
    }

}