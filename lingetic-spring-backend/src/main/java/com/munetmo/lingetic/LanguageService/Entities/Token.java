package com.munetmo.lingetic.LanguageService.Entities;

public record Token(TokenType type, String value) {
    public Token {
        if (value.isBlank()) {
            throw new IllegalArgumentException("value cannot be blank");
        }
    }
}
