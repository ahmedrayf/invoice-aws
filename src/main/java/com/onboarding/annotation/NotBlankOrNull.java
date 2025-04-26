package com.onboarding.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotBlankOrNullValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankOrNull {
    String message() default "Field must not be null or blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
