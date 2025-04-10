package com.onboarding.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@SuperBuilder
@JsonInclude(value= JsonInclude.Include.NON_EMPTY, content= JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse<T> {

    private HttpStatus httpStatus;
    private String message;
    private LocalDateTime timestamp;
    private String exception;
    private Object errors;
    private T  body;

}