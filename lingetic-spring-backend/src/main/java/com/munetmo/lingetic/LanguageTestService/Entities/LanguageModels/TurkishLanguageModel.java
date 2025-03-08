package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;

import java.util.Arrays;
import java.util.Locale;

public final class TurkishLanguageModel implements LanguageModel {
    @Override
    public Language getLanguage() {
        return Language.Turkish;
    }

    private static final String turkishSpecificCharacters = "çğıöşü";

    @Override
    public boolean areEquivalent(String s1, String s2) {
        var normalized1 = normalizeString(s1);
        var normalized2 = normalizeString(s2);

        return normalized1.equals(normalized2);
    }

    private String normalizeString(String input) {
        var words = input.split("\\s+");
        var normalizedWords = Arrays.stream(words)
                .map(w -> w.trim().toLowerCase(Locale.forLanguageTag("tr-TR")))
                .map(w -> {
                    var regex = String.format("^[^a-z0-9%s]+|[^a-z0-9%s]+$", turkishSpecificCharacters, turkishSpecificCharacters);

                    return w.replaceAll(regex, "");
                })
                .filter(w -> !w.isBlank());

        return String.join(" ", normalizedWords.toList());
    }
}