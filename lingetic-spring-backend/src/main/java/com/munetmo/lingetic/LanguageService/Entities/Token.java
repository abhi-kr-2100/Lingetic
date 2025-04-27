package com.munetmo.lingetic.LanguageService.Entities;

public record Token(TokenType type, String value, int sequenceNumber) {
    public Token {
        if (value.isBlank()) {
            throw new IllegalArgumentException("value cannot be blank");
        }
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("sequenceNumber must be positive");
        }
    }
    
    public Token(TokenType type, String value) {
        this(type, value, 1);
    }
}
