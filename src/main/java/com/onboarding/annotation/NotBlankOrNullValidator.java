package com.onboarding.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankOrNullValidator implements ConstraintValidator<NotBlankOrNull, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !value.trim().isEmpty();
    }
}
