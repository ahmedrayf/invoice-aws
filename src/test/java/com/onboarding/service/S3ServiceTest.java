package com.onboarding.service;

import com.onboarding.service.aws.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest

{


    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @Test
    void getFileInputStream_ShouldWrapS3Response() throws IOException  {
        // Arrange
        String testContent = "9999999999|;1234567890|;03.01.2024|;26.11.2023|;25.12.2023|;0800 / 1071020|;73730|;Esslingen|;Teststr. 10 /1|;Firma|;XXX GmbH|;|;|;EUR|;19%|;183.97|;154.60|;29.37|;566655555|;99999999|;03|;|;00281|;false|;0.000000|;11.12.2023|;DE999999999999999999|;XXXXX66XXX|;DE04CCB0009999988A1019999991E002|;\n" +
                "9999999998|;1234567899|;03.01.2024|;26.11.2023|;25.12.2023|;0800 / 1071020|;74564|;Crailsheim|;Musterplatz 1|;Firma|;Stiftung XYV |;|;|;EUR|;19%|;33.22|;27.92|;5.30|;5566666655|;99999999|;14|;|;00192|;false|;0.000000|;14.12.2023|;DE999999999999999999|;XXXXX66XXX|;DE04CCB0009999999A1019999991E002|;";

        ResponseInputStream<GetObjectResponse> mockResponse =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        new ByteArrayInputStream(testContent.getBytes()));

        when(s3Client.getObject((GetObjectRequest) any())).thenReturn(mockResponse);

        // Act
        try (InputStream result = s3Service.getFileInputStream("invoice_20250123.csv")) {
            // Assert
            assertNotNull(result);
            assertEquals(testContent, new String(result.readAllBytes()));
        }
    }

    @Test
    void getFileInputStream_ShouldWrapS3ExceptionAsIOException() {
        when(s3Client.getObject((GetObjectRequest) any())).thenThrow(S3Exception.builder().message("Error").build());

        assertThrows(IOException.class, () -> s3Service.getFileInputStream("invoice_20250123.csv"));
    }
}
