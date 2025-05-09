package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class LatinScriptLanguageModelHelper {
    private static final String TERMINAL_PUNCTUATION = ".?!;,:-";
    private static final String SPECIFIC_CHARACTERS = "àâäæçéèêëîïôœùûüÿçğıöşüåäöāīūēō";
    private static final String SURROUNDING_PUNCTUATION = "'\"«»";

    private static final Pattern LEADING_PUNCTUATION_PATTERN = Pattern.compile(
            "^([%s%s]+)".formatted(SURROUNDING_PUNCTUATION, TERMINAL_PUNCTUATION));
    private static final Pattern TRAILING_PUNCTUATION_PATTERN = Pattern.compile(
            "([%s%s]+)$".formatted(SURROUNDING_PUNCTUATION, TERMINAL_PUNCTUATION));

    private final Locale locale;

    public LatinScriptLanguageModelHelper(Locale locale) {
        this.locale = locale;
    }

    public boolean areEquivalent(String s1, String s2) {
        String n1 = normalize(s1);
        String n2 = normalize(s2);
        return n1.equals(n2);
    }

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        if (input.isBlank()) return tokens;

        int sequenceNumber = 1;
        for (var part : input.split("\\s+")) {
            if (part.isBlank()) continue;

            var leadingPunct = getLeadingPunctuations(part);
            for (var punct : leadingPunct) {
                tokens.add(new Token(TokenType.Punctuation, punct, sequenceNumber++));
            }
            part = part.substring(leadingPunct.size());
            if (part.isBlank()) continue;

            var trailingPunct = getTrailingPunctuations(part);
            part = part.substring(0, part.length() - trailingPunct.size());
            if (part.isBlank()) {
                for (var punct : trailingPunct) {
                    tokens.add(new Token(TokenType.Punctuation, punct, sequenceNumber++));
                }
                continue;
            }

            if (containsLetter(part)) {
                tokens.add(new Token(TokenType.Word, part, sequenceNumber++));
            } else if (containsDigit(part)) {
                tokens.add(new Token(TokenType.Number, part, sequenceNumber++));
            } else {
                tokens.add(new Token(TokenType.Punctuation, part, sequenceNumber++));
            }

            for (var punct : trailingPunct) {
                tokens.add(new Token(TokenType.Punctuation, punct, sequenceNumber++));
            }
        }
        return tokens;
    }

    /**
     * For now, combineTokens only correctly combines one level of nesting.
     * Example: `He said, "Hello, world!"` works.
     *          `He said, "She said, "Hello, world!"" does not work.
     */
    public String combineTokens(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        Token previousToken = null;
        boolean inQuote = false;

        for (var token : tokens) {
            if (previousToken != null) {
                if (previousToken.type() == TokenType.Word || previousToken.type() == TokenType.Number) {
                    if (token.type() == TokenType.Word || token.type() == TokenType.Number) {
                        // space between words/numbers
                        result.append(" ");
                    } else if (SURROUNDING_PUNCTUATION.contains(token.value())) {
                        if (!inQuote) {
                            inQuote = true;
                            // space before opening quote/other surrounding punctuation
                            result.append(" ");
                        } else {
                            inQuote = false;
                        }
                    }
                } else if (token.type() == TokenType.Word || token.type() == TokenType.Number) {
                    if (SURROUNDING_PUNCTUATION.contains(previousToken.value())) {
                        if (!inQuote) {
                            // space after closing quote/other surrounding punctuation
                            result.append(" ");
                        }
                    } else {
                        // space between punctuation
                        result.append(" ");
                    }
                } else if (SURROUNDING_PUNCTUATION.contains(previousToken.value())) {
                    if (!inQuote) {
                        // space after closing quote/other surrounding punctuation
                        result.append(" ");
                    }
                } else if (SURROUNDING_PUNCTUATION.contains(token.value())) {
                    if (!inQuote) {
                        inQuote = true;
                        // space before opening quote/other surrounding punctuation
                        result.append(" ");
                    } else {
                        inQuote = false;
                    }
                }
            }

            result.append(token.value());
            previousToken = token;
        }

        return result.toString().trim();
    }

    private List<String> getLeadingPunctuations(String input) {
        var matcher = LEADING_PUNCTUATION_PATTERN.matcher(input);
        if (matcher.find()) {
            return List.of(matcher.group(1).split(""));
        }
        return List.of();
    }

    private List<String> getTrailingPunctuations(String input) {
        var matcher = TRAILING_PUNCTUATION_PATTERN.matcher(input);
        if (matcher.find()) {
            return List.of(matcher.group(1).split(""));
        }
        return List.of();
    }

    private String normalize(String input) {
        var words = input.split("\\s+");
        var cleaned = Arrays.stream(words)
                .map(w -> w.trim().toLowerCase(locale))
                .map(w -> {
                    String regex = String.format("^[^a-z0-9%s]+|[^a-z0-9%s]+$",
                            SPECIFIC_CHARACTERS, SPECIFIC_CHARACTERS);
                    return w.replaceAll(regex, "");
                })
                .filter(w -> !w.isBlank());
        return String.join(" ", cleaned.toList());
    }

    private boolean containsLetter(String s) {
        return s.chars().anyMatch(Character::isLetter);
    }

    private boolean containsDigit(String s) {
        return s.chars().anyMatch(Character::isDigit);
    }
}
