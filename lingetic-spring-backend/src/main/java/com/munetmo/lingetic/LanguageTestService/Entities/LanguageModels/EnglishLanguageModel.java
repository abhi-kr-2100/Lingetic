package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;

import java.util.Arrays;
import java.util.Locale;

public final class EnglishLanguageModel implements LanguageModel {
    @Override
    public Language getLanguage() {
        return Language.English;
    }

    @Override
    public boolean areEquivalent(String s1, String s2) {
        var normalized1 = normalizeString(s1);
        var normalized2 = normalizeString(s2);

        return normalized1.equals(normalized2);
    }

    private String normalizeString(String input) {
        var words = input.split("\\s+");
        var normalizedWords = Arrays.stream(words)
                .map(w -> w.trim().toLowerCase(Locale.ENGLISH))
                .map(w -> w.replaceAll("^[^a-z0-9]+|[^a-z0-9]+$", ""))
                .filter(w -> !w.isBlank());

        var normalized = String.join(" ", normalizedWords.toList());
        
        return normalized;
    }
}
