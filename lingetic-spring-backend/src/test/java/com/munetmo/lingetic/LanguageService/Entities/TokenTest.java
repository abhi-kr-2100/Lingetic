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
        assertNull(word.startIndex());

        assertEquals(TokenType.Punctuation, punctuation.type());
        assertEquals("!", punctuation.value());
        assertNull(punctuation.startIndex());

        assertEquals(TokenType.Number, intNumber.type());
        assertEquals("42", intNumber.value());
        assertNull(intNumber.startIndex());

        assertEquals(TokenType.Number, decimalNumber.type());
        assertEquals("3.14", decimalNumber.value());
        assertNull(decimalNumber.startIndex());
    }

    @Test
    void constructorWithStartIndexShouldCreateValidInstance() {
        var word = new Token(TokenType.Word, "hello", 5);
        var punctuation = new Token(TokenType.Punctuation, "!", 10);
        
        assertEquals(TokenType.Word, word.type());
        assertEquals("hello", word.value());
        assertEquals(5, word.startIndex());
        
        assertEquals(TokenType.Punctuation, punctuation.type());
        assertEquals("!", punctuation.value());
        assertEquals(10, punctuation.startIndex());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenValueIsBlank(String value) {
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, value));
    }
    
    @Test
    void constructorShouldThrowExceptionWhenStartIndexIsNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, "hello", -1));
        assertThrows(IllegalArgumentException.class,
            () -> new Token(TokenType.Word, "hello", -42));
    }

    @Test
    void constructorShouldHandleNullStartIndex() {
        var word = new Token(TokenType.Word, "hello", null);
        var punctuation = new Token(TokenType.Punctuation, "!", null);

        assertNull(word.startIndex());
        assertNull(punctuation.startIndex());
    }
}
