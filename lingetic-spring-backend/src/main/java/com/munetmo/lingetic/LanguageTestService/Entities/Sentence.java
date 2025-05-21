package com.munetmo.lingetic.LanguageTestService.Entities;

import com.munetmo.lingetic.LanguageService.Entities.Language;

import java.util.List;
import java.util.UUID;

public record Sentence(
    UUID id,
    Language sourceLanguage,
    String sourceText,
    Language translationLanguage,
    String translationText,
    List<WordExplanation> sourceWordExplanation
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
        String translationText,
        List<WordExplanation> sourceWordExplanations
    ) {
        return new Sentence(
            UUID.randomUUID(),
            sourceLanguage,
            sourceText,
            translationLanguage,
            translationText,
            sourceWordExplanations
        );
    }

    public static Sentence create(
        Language sourceLanguage,
        String sourceText,
        Language translationLanguage,
        String translationText
    ) {
        return create(sourceLanguage, sourceText, translationLanguage, translationText, List.of());
    }
}
