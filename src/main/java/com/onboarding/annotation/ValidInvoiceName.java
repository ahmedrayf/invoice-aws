package com.onboarding.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InvoiceNameValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInvoiceName {
    String message() default "Invalid invoice name format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
