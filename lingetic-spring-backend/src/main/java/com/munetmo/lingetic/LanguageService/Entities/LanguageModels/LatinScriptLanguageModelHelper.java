package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class LatinScriptLanguageModelHelper {
    private static final String SPECIFIC_CHARACTERS = "àâäæçéèêëîïôœùûüÿçğıöşüåäöāīūēō";
    private static final String SURROUNDING_PUNCTUATION = "'\"«»";

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

        int currentPos = 0;
        int length = input.length();
        var currentPart = new StringBuilder();

        while (currentPos < length) {
            var c = input.charAt(currentPos);

            // Tokens are separated by whitespace but punctuations need to be handled differently
            // He said, "Hello, world!" separated just by whitespace would be:
            // [He; said,; "Hello,; world!"]

            if (Character.isWhitespace(c)) {
                if (!currentPart.isEmpty()) {
                    var token = getAppropriateToken(currentPart.toString(), currentPos - currentPart.length());
                    tokens.add(token);
                    currentPart.setLength(0);
                }
                currentPos++;
                continue;
            }

            var previousChar = currentPos - 1 >= 0 ? input.charAt(currentPos - 1) : null;
            var nextChar = currentPos + 1 < length ? input.charAt(currentPos + 1) : null;
            if (isStandalonePunctuation(c, previousChar, nextChar)) {
                if (!currentPart.isEmpty()) {
                    var token = getAppropriateToken(currentPart.toString(), currentPos - currentPart.length());
                    tokens.add(token);
                    currentPart.setLength(0);
                }

                tokens.add(new Token(TokenType.Punctuation, String.valueOf(c), currentPos));
                currentPos++;
                continue;
            }

            currentPart.append(c);
            currentPos++;
        }

        if (!currentPart.isEmpty()) {
            var token = getAppropriateToken(currentPart.toString(), currentPos - currentPart.length());
            tokens.add(token);
        }

        return tokens;
    }

    private Token getAppropriateToken(String value, int startIndex) {
        if (containsLetter(value)) {
            return new Token(TokenType.Word, value, startIndex);
        } else if (containsDigit(value)) {
            return new Token(TokenType.Number, value, startIndex);
        } else {
            return new Token(TokenType.Punctuation, value, startIndex);
        }
    }

    private boolean isStandalonePunctuation(char currentChar, @Nullable Character previousChar, @Nullable Character nextChar) {
        if (Character.isLetter(currentChar) || Character.isDigit(currentChar)) {
            return false; // not a punctuation
        }

        if (previousChar == null || nextChar == null) {
            return true; // trailing punctuation
        }

        if (Character.isWhitespace(previousChar) || Character.isWhitespace(nextChar)) {
            return true; // trailing punctuation
        }

        if (isPunctuation(previousChar) || isPunctuation(nextChar)) {
            // adjacent to a punctuations is a punctuation: "...", "?!", '"world!"' etc.
            return true;
        }

        return false;
    }

    private boolean isPunctuation(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
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
