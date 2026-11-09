package com.futapp.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BirthDateValidatorTests {
    private final BirthDateValidator validator = new BirthDateValidator();

    @Test
    void aceptaUnaFechaRealDentroDelRango() {
        assertThat(validator.isValid("2000-02-29", null)).isTrue();
    }

    @Test
    void rechazaFormatoInvalidoFechaImposibleYRangoInvalido() {
        assertThat(validator.isValid("111111-11-11", null)).isFalse();
        assertThat(validator.isValid("2001-02-29", null)).isFalse();
        assertThat(validator.isValid("1899-12-31", null)).isFalse();
        assertThat(validator.isValid(LocalDate.now().plusDays(1).toString(), null)).isFalse();
    }
}
