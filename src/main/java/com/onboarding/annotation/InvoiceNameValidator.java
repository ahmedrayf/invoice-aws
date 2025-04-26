package com.onboarding.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

public class InvoiceNameValidator implements ConstraintValidator<ValidInvoiceName, String> {

    @Value("${invoice.filename-pattern}")
    private String filenamePattern;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return value.matches(filenamePattern);
    }

}
