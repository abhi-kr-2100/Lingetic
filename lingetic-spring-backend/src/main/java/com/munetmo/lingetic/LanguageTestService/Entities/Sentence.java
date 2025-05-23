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
    int difficulty,
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
}
