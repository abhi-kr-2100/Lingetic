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
        assertEquals(1, word.sequenceNumber());

        assertEquals(TokenType.Punctuation, punctuation.type());
        assertEquals("!", punctuation.value());
        assertEquals(1, punctuation.sequenceNumber());

        assertEquals(TokenType.Number, intNumber.type());
        assertEquals("42", intNumber.value());
        assertEquals(1, intNumber.sequenceNumber());

        assertEquals(TokenType.Number, decimalNumber.type());
        assertEquals("3.14", decimalNumber.value());
        assertEquals(1, decimalNumber.sequenceNumber());
    }

    @Test
    void constructorWithSequenceNumberShouldCreateValidInstance() {
        var word = new Token(TokenType.Word, "hello", 5);
        var punctuation = new Token(TokenType.Punctuation, "!", 10);
        
        assertEquals(TokenType.Word, word.type());
        assertEquals("hello", word.value());
        assertEquals(5, word.sequenceNumber());
        
        assertEquals(TokenType.Punctuation, punctuation.type());
        assertEquals("!", punctuation.value());
        assertEquals(10, punctuation.sequenceNumber());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenValueIsBlank(String value) {
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, value));
    }
    
    @Test
    void constructorShouldThrowExceptionWhenSequenceNumberIsZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, "hello", 0));
    }
    
    @Test
    void constructorShouldThrowExceptionWhenSequenceNumberIsNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, "hello", -1));
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, "hello", -42));
    }
}
