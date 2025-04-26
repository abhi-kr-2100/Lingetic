package com.munetmo.lingetic.LanguageService.Entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {
    @Test
    void constructorShouldCreateValidInstance() {
        var word = new Token(TokenType.Word, "hello");
        var punctuation = new Token(TokenType.Punctuation, "!");
        var intNumber = new Token(TokenType.Number, "42");
        var decimalNumber = new Token(TokenType.Number, "3.14");

        assertEquals(TokenType.Word, word.type());
        assertEquals("hello", word.value());

        assertEquals(TokenType.Punctuation, punctuation.type());
        assertEquals("!", punctuation.value());

        assertEquals(TokenType.Number, intNumber.type());
        assertEquals("42", intNumber.value());

        assertEquals(TokenType.Number, decimalNumber.type());
        assertEquals("3.14", decimalNumber.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenValueIsBlank(String value) {
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, value));
    }
}
