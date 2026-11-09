package com.futapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BirthDateValidator.class)
public @interface ValidBirthDate {
    String message() default "La fecha de nacimiento debe ser válida, no futura y posterior a 1900";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
