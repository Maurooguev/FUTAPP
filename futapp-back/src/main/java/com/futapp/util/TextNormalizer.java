package com.futapp.util;

import java.util.Locale;

public final class TextNormalizer {
    private static final Locale SPANISH = Locale.forLanguageTag("es-AR");

    private TextNormalizer() {}

    public static String titleCase(String value) {
        if (value == null) return null;
        String normalized = value.trim().replaceAll("\\s+", " ").toLowerCase(SPANISH);
        if (normalized.isEmpty()) return normalized;
        StringBuilder result = new StringBuilder(normalized.length());
        boolean capitalize = true;
        for (int index = 0; index < normalized.length();) {
            int codePoint = normalized.codePointAt(index);
            result.appendCodePoint(capitalize && Character.isLetter(codePoint)
                    ? Character.toTitleCase(codePoint) : codePoint);
            capitalize = !Character.isLetterOrDigit(codePoint);
            index += Character.charCount(codePoint);
        }
        return result.toString();
    }
}
