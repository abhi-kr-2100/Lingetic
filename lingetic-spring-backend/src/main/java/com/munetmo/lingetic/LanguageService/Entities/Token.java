package com.munetmo.lingetic.LanguageService.Entities;

import org.jspecify.annotations.Nullable;

public record Token(TokenType type, String value, @Nullable Integer startIndex) {
    public Token {
        if (value.isBlank()) {
            throw new IllegalArgumentException("value cannot be blank");
        }

        if (startIndex != null && startIndex < 0) {
            throw new IllegalArgumentException("startIndex must be non-negative");
        }
    }
    
    public Token(TokenType type, String value) {
        this(type, value, null);
    }
}
