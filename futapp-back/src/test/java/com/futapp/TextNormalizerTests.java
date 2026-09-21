package com.futapp;

import com.futapp.util.TextNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextNormalizerTests {
    @Test
    void normalizaPalabrasYEspacios() {
        assertEquals("Liga Amateur Buenos Aires", TextNormalizer.titleCase("  LIGA   amateur BUENOS AIRES "));
        assertEquals("Atlético San-Martín", TextNormalizer.titleCase("atlÉTICO san-martÍn"));
        assertEquals("D'Angelo", TextNormalizer.titleCase("D'ANGELO"));
    }

    @Test
    void conservaNulos() {
        assertNull(TextNormalizer.titleCase(null));
    }
}
