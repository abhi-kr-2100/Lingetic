package com.munetmo.lingetic.LanguageTestService.Entities;

import com.munetmo.lingetic.LanguageService.Entities.Language;

import java.util.Objects;
import java.util.UUID;

public record Sentence(
    UUID id,
    Language sourceLanguage,
    String sourceText,
    Language translationLanguage,
    String translationText
) {
    public Sentence {
        if (sourceText.isBlank()) {
            throw new IllegalArgumentException("Source text cannot be blank");
        }

        if (translationText.isBlank()) {
            throw new IllegalArgumentException("Translation text cannot be blank");
        }
    }

    public static Sentence create(
        Language sourceLanguage,
        String sourceText,
        Language translationLanguage,
        String translationText
    ) {
        return new Sentence(
            UUID.randomUUID(),
            sourceLanguage,
            sourceText,
            translationLanguage,
            translationText
        );
    }
}
