package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;

import java.util.Arrays;
import java.util.Locale;

public final class FrenchLanguageModel implements LanguageModel {
    @Override
    public Language getLanguage() {
        return Language.French;
    }

    private static final String frenchSpecificCharacters = "àâäæçéèêëîïôœùûüÿ";

    @Override
    public boolean areEquivalent(String s1, String s2) {
        var normalized1 = normalizeString(s1);
        var normalized2 = normalizeString(s2);

        return normalized1.equals(normalized2);
    }

    private String normalizeString(String input) {
        var regex = String.format("^[^a-z0-9%s]+|[^a-z0-9%s]+$", frenchSpecificCharacters,
                frenchSpecificCharacters);

        var words = input.split("\\s+");
        var normalizedWords = Arrays.stream(words)
                .map(w -> w.trim().toLowerCase(Locale.FRANCE))
                .map(w -> w.replaceAll(regex, ""))
                .filter(w -> !w.isBlank());

        return String.join(" ", normalizedWords.toList());
    }
}
