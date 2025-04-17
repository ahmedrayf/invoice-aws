package com.onboarding.service;

import com.onboarding.handler.ResourceNotFoundException;
import com.onboarding.service.aws.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    @InjectMocks
    private S3Service s3Service;
    private static final String INVOICE_NAME = "invoice_20250301.csv";

    @Test
    void getFileInputStream_ShouldWrapS3Response() throws IOException  {
        // Arrange
        File file = ResourceUtils.getFile("src/test/resources/invoices/success/csv/invoice_20250301.csv");
        String content = Files.readString(file.toPath());

        ResponseInputStream<GetObjectResponse> mockResponse =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        new ByteArrayInputStream(content.getBytes()));

        when(s3Client.getObject((GetObjectRequest) any())).thenReturn(mockResponse);

        // Act
        try (InputStream result = s3Service.getFileInputStream(INVOICE_NAME)) {
            // Assert
            assertNotNull(result);
            assertEquals(content, new String(result.readAllBytes()));
        }
    }

    @Test
    void getFileInputStream_ShouldWrapS3ExceptionAsResourceException() {
        when(s3Client.getObject((GetObjectRequest) any())).thenThrow(S3Exception.builder().message("Error").build());

        assertThrows(ResourceNotFoundException.class, () -> s3Service.getFileInputStream(INVOICE_NAME));
    }
}
