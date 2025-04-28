package com.onboarding.handler;

import com.onboarding.dto.response.ApiResponse;
import com.onboarding.exception.InvoiceProcessingException;
import com.onboarding.exception.MessageProcessingException;
import com.onboarding.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleInvoiceProcessingException() {
        InvoiceProcessingException ex = new InvoiceProcessingException("Invoice error");
        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleInvoiceProcessingException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invoice error", response.getBody().getMessage());
    }

    @Test
    void handleMessageProcessingException() {
        MessageProcessingException ex = new MessageProcessingException("SQS error");
        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleMessageProcessingException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("SQS error", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleIOException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void handleInterruptedException() {
        InterruptedException ex = new InterruptedException("Interrupted");
        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleInterruptedException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Processing was interrupted", response.getBody().getMessage());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void handleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Validation error");

        ConstraintViolationException ex = new ConstraintViolationException(
                "Constraint violation", Collections.singleton(violation));

        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", response.getBody().getMessage());
    }

    @Test
    void handleExecutionExceptionWithInvoiceProcessingException() {
        ExecutionException ex = new ExecutionException(
                new InvoiceProcessingException("Invoice processing failed"));

        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleExecutionException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invoice processing failed", response.getBody().getMessage());
    }

    @Test
    void handleExecutionExceptionWithResourceNotFoundException() {
        ExecutionException ex = new ExecutionException(
                new ResourceNotFoundException("Resource not found"));

        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleExecutionException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    void handleExecutionExceptionWithGenericException() {
        ExecutionException ex = new ExecutionException(
                new RuntimeException("Generic error"));

        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleExecutionException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred.", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValidException(){
        // Create test data
        FieldError fieldError = new FieldError("object", "field", "default message");

        // Create mock objects
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // Execute
        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleValidationException(ex);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        Map<String, String> errors = (Map<String, String>) response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("default message", errors.get("field"));
    }


    @Test
    void handleMethodArgumentNotValidException_withNullMessage() {
        FieldError fieldError = new FieldError("object", "field", null);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleValidationException(ex);

        Map<String, String> errors = (Map<String, String>) response.getBody().getErrors();
        assertEquals("Invalid value", errors.get("field"));
    }

    @Test
    void handleAllExceptions() {
        Exception ex = new Exception("Generic exception");
        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void buildErrorResponse() {
        ResponseEntity<ApiResponse<String>> response =
                globalExceptionHandler.buildErrorResponse(HttpStatus.BAD_REQUEST, "Test error");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

}
