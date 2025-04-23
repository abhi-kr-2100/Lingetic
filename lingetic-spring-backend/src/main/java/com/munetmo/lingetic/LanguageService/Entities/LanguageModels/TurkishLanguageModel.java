package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class TurkishLanguageModel implements LanguageModel {
    @Override
    public Language getLanguage() {
        return Language.Turkish;
    }

    private static final String turkishSpecificCharacters = "çğıöşü";
    private static final String terminalPunctuation = ".?!;,:";
    private static final String surroundingPunctuation = "\'\"";

    @Override
    public boolean areEquivalent(String s1, String s2) {
        var normalized1 = normalizeString(s1);
        var normalized2 = normalizeString(s2);

        return normalized1.equals(normalized2);
    }

    @Override
    public List<Token> tokenize(String input) {
        var tokens = new ArrayList<Token>();

        if (input.isBlank()) {
            return tokens;
        }

        var parts = input.split("\\s+");
        for (var part : parts) {
            if (part.isBlank()) {
                continue;
            }

            var firstChar = part.substring(0, 1);
            if (surroundingPunctuation.contains(firstChar)) {
                tokens.add(new Token(TokenType.Punctuation, firstChar));
                part = part.substring(1);
            }

            if (part.isBlank()) {
                continue;
            }

            @Nullable Token lastToken = null;
            var lastChar = part.substring(part.length() - 1);
            if (surroundingPunctuation.contains(lastChar) || terminalPunctuation.contains(lastChar)) {
                lastToken = new Token(TokenType.Punctuation, lastChar);
                part = part.substring(0, part.length() - 1);
            }

            if (part.isBlank()) {
                tokens.add(lastToken);
                continue;
            }

            if (containsLetter(part)) {
                tokens.add(new Token(TokenType.Word, part));
            } else if (containsDigit(part)) {
                tokens.add(new Token(TokenType.Number, part));
            } else {
                tokens.add(new Token(TokenType.Punctuation, part));
            }

            if (lastToken != null) {
                tokens.add(lastToken);
            }
        }

        return tokens;
    }

    private String normalizeString(String input) {
        var words = input.split("\\s+");
        var normalizedWords = Arrays.stream(words)
                .map(w -> w.trim().toLowerCase(Locale.forLanguageTag("tr-TR")))
                .map(w -> {
                    var regex = String.format("^[^a-z0-9%s]+|[^a-z0-9%s]+$", turkishSpecificCharacters,
                            turkishSpecificCharacters);

                    return w.replaceAll(regex, "");
                })
                .filter(w -> !w.isBlank());

        return String.join(" ", normalizedWords.toList());
    }

    private boolean containsLetter(String input) {
        return input.chars().anyMatch(Character::isLetter);
    }

    private boolean containsDigit(String input) {
        return input.chars().anyMatch(Character::isDigit);
    }
}
