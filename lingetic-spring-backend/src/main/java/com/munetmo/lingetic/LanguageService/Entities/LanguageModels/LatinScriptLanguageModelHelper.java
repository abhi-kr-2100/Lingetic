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
            char c = input.charAt(currentPos);

            // Tokens are separated by whitespace but punctuations need to be handled differently
            // He said, "Hello, world!" separated just by whitespace would be:
            // [He; said,; "Hello,; world!"]

            if (Character.isWhitespace(c)) {
                if (!currentPart.isEmpty()) {
                    processCurrentPart(tokens, currentPart.toString(), currentPos - currentPart.length());
                    currentPart.setLength(0);
                }
                currentPos++;
                continue;
            }

            var currentStr = currentPart.toString();
            var nextChar = currentPos + 1 < length ? input.charAt(currentPos + 1) : null;
            if (isPunctuationLike(c) &&
                    // "I'm" should not be split into "I"; "'"; "m"
                    // However, "I'm." should be split into "I'm" and "."
                    // "1,50" should not be split into "1"; ","; "50"
                    // "I'm 10." should be split into "I'm"; "10"; "."
                    !((containsLetter(currentStr) || containsDigit(currentStr)) && nextChar != null && !isPunctuationLike(nextChar))
            ) {
                if (!currentPart.isEmpty()) {
                    processCurrentPart(tokens, currentPart.toString(), currentPos - currentPart.length());
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
            processCurrentPart(tokens, currentPart.toString(), currentPos - currentPart.length());
        }

        return tokens;
    }

    private void processCurrentPart(List<Token> tokens, String part, int startIndex) {
        if (containsLetter(part)) {
            tokens.add(new Token(TokenType.Word, part, startIndex));
        } else if (containsDigit(part)) {
            tokens.add(new Token(TokenType.Number, part, startIndex));
        } else {
            tokens.add(new Token(TokenType.Punctuation, part, startIndex));
        }
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

    private boolean isLetterLike(@Nullable Character c) {
        return c != null && (Character.isLetter(c) || Character.isDigit(c));
    }

    private boolean isDigitLike(@Nullable Character c) {
        return c != null && Character.isDigit(c);
    }

    private boolean isPunctuationLike(@Nullable Character c) {
        return c != null && !isLetterLike(c) && !isDigitLike(c);
    }

    private boolean containsLetter(String s) {
        return s.chars().anyMatch(Character::isLetter);
    }

    private boolean containsDigit(String s) {
        return s.chars().anyMatch(Character::isDigit);
    }
}
