package com.onboarding.handler;

import com.onboarding.dto.response.ApiResponse;
import com.onboarding.exception.InvoiceProcessingException;
import com.onboarding.exception.MessageProcessingException;
import com.onboarding.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvoiceProcessingException.class)
    public ResponseEntity<ApiResponse<String>> handleInvoiceProcessingException(
            InvoiceProcessingException ex) {
        log.error("Invoice processing failed: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MessageProcessingException.class)
    public ResponseEntity<ApiResponse<String>> handleMessageProcessingException(
            MessageProcessingException ex) {
        log.error("Error when sending message to sqs: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleIOException(
            ResourceNotFoundException ex) {
        log.error("Source not found: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ApiResponse<String>> handleInterruptedException(
            InterruptedException ex) {
        Thread.currentThread().interrupt();
        log.error("Processing interrupted: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Processing was interrupted");
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.error("ConstraintViolationException: {}", ex.getMessage(), ex);
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage) //
                .findFirst()
                .orElse("Validation error");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<ApiResponse<String>> handleExecutionException(ExecutionException ex) {
        Throwable rootCause = ex.getCause();
        String errorMsg;

        if (rootCause instanceof InvoiceProcessingException || rootCause instanceof ResourceNotFoundException)
            errorMsg = rootCause.getMessage();
        else
            errorMsg = "An unexpected error occurred.";

        log.error(errorMsg, rootCause);


        HttpStatus status = rootCause instanceof ResourceNotFoundException
                ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;
        if (rootCause instanceof InvoiceProcessingException)
            status = HttpStatus.BAD_REQUEST;


        return buildErrorResponse(status, errorMsg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ?
                                error.getDefaultMessage() : "Invalid value"
                ));

        log.warn("Validation errors: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.<String>builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .message("Validation failed")
                        .errors(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
    }

    ResponseEntity<ApiResponse<String>> buildErrorResponse(
            HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<String>builder()
                        .httpStatus(status)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}

