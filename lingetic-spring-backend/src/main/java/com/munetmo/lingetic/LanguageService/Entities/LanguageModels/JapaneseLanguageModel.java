package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import com.worksap.nlp.sudachi.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JapaneseLanguageModel implements LanguageModel, AutoCloseable {
    /**
     * Sudachi's tokenizer often fails due to transient issues. The SafeTokenizer class wraps
     * tokenizer calls in a retry loop.
     */
    private static class SafeTokenizer implements AutoCloseable {
        @Nullable
        private Dictionary dictionary = null;

        @Nullable
        private Tokenizer tokenizer = null;

        public void reinit() {
            if (dictionary != null) {
                try {
                    dictionary.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to close dictionary: " + e.getMessage(), e);
                }
            }

            try {
                var config = Config.fromClasspath("sudachi/sudachi.json");
                dictionary = new DictionaryFactory().create(config);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create dictionary: " + e.getMessage(), e);
            }
            tokenizer = dictionary.create();
        }

        public SafeTokenizer() throws IOException {
            reinit();
        }

        public MorphemeList tokenize(String input) {
            // Sudachi doesn't handle whitespace properly
            // Thankfully, Japanese is a whitespace-free language
            input = input.replaceAll("\\s+", "").trim();

            if (tokenizer == null) {
                reinit();
            }

            if (tokenizer == null) {
                throw new IllegalStateException("Impossible state: tokenizer is null even after init");
            }

            final int maxRetries = 10;
            int retryCount = 0;
            while (true) {
                try {
                    return tokenizer.tokenize(Tokenizer.SplitMode.C, input);
                } catch (Exception e) {
                    if (++retryCount > maxRetries) {
                        throw e;
                    }

                    try {
                        reinit();
                        Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (dictionary != null) {
                dictionary.close();
            }
        }
    }

    private final SafeTokenizer tokenizer;

    public JapaneseLanguageModel() throws IOException {
        tokenizer = new SafeTokenizer();
    }

    @Override
    public void close() throws IOException {
        tokenizer.close();
    }

    @Override
    public Language getLanguage() {
        return Language.Japanese;
    }

    @Override
    public boolean areEquivalent(String s1, String s2) {
        String normalized1 = normalizeString(s1);
        String normalized2 = normalizeString(s2);
        return normalized1.equals(normalized2);
    }

    @Override
    public List<Token> tokenize(String input) {
        if (input.isBlank()) {
            return List.of();
        }

        List<Token> tokens = new ArrayList<>();
        var morphemes = tokenizer.tokenize(input);
        int sequenceNumber = 1;

        for (var morpheme : morphemes) {
            var surface = morpheme.surface();
            if (surface.matches(".*[^\\p{N}\\p{P}\\p{S}].*")) {
                // Contains any non-digit and non-symbol character -> Word
                tokens.add(new Token(TokenType.Word, surface, sequenceNumber++));
            } else if (surface.matches(".*\\p{N}.*")) {
                // Otherwise, Contains any digit character -> Number
                tokens.add(new Token(TokenType.Number, surface, sequenceNumber++));
            } else {
                // Must be only punctuation/symbols
                for (int i = 0; i < surface.length(); i++) {
                    tokens.add(new Token(TokenType.Punctuation, String.valueOf(surface.charAt(i)), sequenceNumber++));
                }
            }
        }

        return tokens;
    }

    private String normalizeString(String input) {
        if (input.isBlank()) {
            return "";
        }

        MorphemeList morphemes = tokenizer.tokenize(input);
        List<String> normalizedParts = new ArrayList<>();

        for (var morpheme : morphemes) {
            var surface = morpheme.surface().trim();
            if (surface.isBlank()) {
                continue;
            }
            // This RegEx matches most Unicode punctuation and symbols,
            // including almost all Japanese ones
            if (!surface.matches("[\\p{P}\\p{S}]+")) {
                normalizedParts.add(surface);
            }
        }

        return String.join(" ", normalizedParts).toLowerCase(Locale.JAPANESE);
    }
}
