package com.futapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DateTimeException;
import java.time.LocalDate;

public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, String> {
    private static final LocalDate EARLIEST_DATE = LocalDate.of(1900, 1, 1);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        try {
            LocalDate date = LocalDate.parse(value);
            return !date.isBefore(EARLIEST_DATE) && !date.isAfter(LocalDate.now());
        } catch (DateTimeException exception) {
            return false;
        }
    }
}
