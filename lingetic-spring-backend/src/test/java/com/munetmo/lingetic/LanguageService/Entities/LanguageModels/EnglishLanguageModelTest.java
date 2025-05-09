package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnglishLanguageModelTest {
    private EnglishLanguageModel model;

    @BeforeEach
    void setUp() {
        model = new EnglishLanguageModel();
    }

    @Test
    void getLanguageShouldReturnEnglish() {
        assertEquals(Language.English, model.getLanguage());
    }

    @Test
    void areEquivalentShouldMatchIdenticalStrings() {
        assertTrue(model.areEquivalent("hello", "hello"));
        assertFalse(model.areEquivalent("hello", "world"));
    }

    @Test
    void areEquivalentShouldIgnoreCase() {
        assertTrue(model.areEquivalent("Hello", "hello"));
        assertTrue(model.areEquivalent("HELLO", "hello"));
        assertTrue(model.areEquivalent("HeLLo", "hEllO"));
    }

    @Test
    void areEquivalentShouldIgnoreLeadingAndTrailingWhitespace() {
        assertTrue(model.areEquivalent("hello ", "hello"));
        assertTrue(model.areEquivalent(" hello", "hello "));
        assertTrue(model.areEquivalent("  hello  ", "hello"));
    }

    @Test
    void areEquivalentShouldWorkWithMultipleWords() {
        assertTrue(model.areEquivalent("hello   world", "hello world"));
        assertTrue(model.areEquivalent("hello\tworld", "hello world"));
        assertTrue(model.areEquivalent("hello\nworld", "hello world"));
        assertTrue(model.areEquivalent("Hello, world!", "hello world"));
        assertFalse(model.areEquivalent("hello world", "goodbye world!"));
    }

    @Test
    void areEquivalentShouldHandleEmptyStrings() {
        assertTrue(model.areEquivalent("", ""));
        assertTrue(model.areEquivalent(" ", " "));
        assertTrue(model.areEquivalent(" ", ""));
    }

    @Test
    void areEquivalentShouldIgnoreLeadingTrailingAndIntraWordPunctuation() {
        assertTrue(model.areEquivalent("hello!", "hello!"));
        assertTrue(model.areEquivalent("hello!", "hello"));
        assertTrue(model.areEquivalent("hello, world", "hello world"));
        assertFalse(model.areEquivalent("I'm", "Im"));
    }

    @Test
    void areEquivalentShouldHandleNumbers() {
        assertTrue(model.areEquivalent("1234", "  1234 "));
    }

    @Test
    void tokenizeShouldTokenizeSimpleSentences() {
        var tokens = model.tokenize("Hello, world!");

        assertEquals(4, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Hello", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals(",", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("world", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldKeepPunctuationBetweenLettersAsOneWord() {
        var tokens = model.tokenize("I'll see you when you've done your work, and I'm free.");

        assertEquals(13, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("I'll", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("see", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("you", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("when", tokens.get(3).value());

        assertEquals(TokenType.Word, tokens.get(4).type());
        assertEquals("you've", tokens.get(4).value());

        assertEquals(TokenType.Word, tokens.get(5).type());
        assertEquals("done", tokens.get(5).value());

        assertEquals(TokenType.Word, tokens.get(6).type());
        assertEquals("your", tokens.get(6).value());

        assertEquals(TokenType.Word, tokens.get(7).type());
        assertEquals("work", tokens.get(7).value());

        assertEquals(TokenType.Punctuation, tokens.get(8).type());
        assertEquals(",", tokens.get(8).value());

        assertEquals(TokenType.Word, tokens.get(9).type());
        assertEquals("and", tokens.get(9).value());

        assertEquals(TokenType.Word, tokens.get(10).type());
        assertEquals("I'm", tokens.get(10).value());

        assertEquals(TokenType.Word, tokens.get(11).type());
        assertEquals("free", tokens.get(11).value());

        assertEquals(TokenType.Punctuation, tokens.get(12).type());
        assertEquals(".", tokens.get(12).value());
    }

    @Test
    void tokenizeShouldTokenizeNumbers() {
        var tokens = model.tokenize("I'm 10.");
        assertEquals(3, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("I'm", tokens.get(0).value());

        assertEquals(TokenType.Number, tokens.get(1).type());
        assertEquals("10", tokens.get(1).value());

        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals(".", tokens.get(2).value());
    }

    @Test
    void tokenizeShouldTreatNumbersWithPunctuationAsNumbers() {
        var tokens = model.tokenize("(123) 1+2 4,500 3.14 4#2");
        assertEquals(5, tokens.size());

        assertEquals(new Token(TokenType.Number, "(123)", 1), tokens.get(0));
        assertEquals(new Token(TokenType.Number, "1+2", 2), tokens.get(1));
        assertEquals(new Token(TokenType.Number, "4,500", 3), tokens.get(2));
        assertEquals(new Token(TokenType.Number, "3.14", 4), tokens.get(3));
        assertEquals(new Token(TokenType.Number, "4#2", 5), tokens.get(4));
    }

    @Test
    void tokenizeShouldTreatAMixOfNumbersAndLettersAsWords() {
        var tokens = model.tokenize("I'm in 10a.");
        assertEquals(4, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("I'm", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("in", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("10a", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals(".", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldHandleIsolatedPunctuations() {
        var tokens = model.tokenize("This is a symbol: .");

        assertEquals(6, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("This", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("is", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("a", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("symbol", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals(":", tokens.get(4).value());

        assertEquals(TokenType.Punctuation, tokens.get(5).type());
        assertEquals(".", tokens.get(5).value());
    }

    @Test
    void shouldHandleConsecutivePunctuationEnding() {
        var tokens = model.tokenize("What?!");

        assertEquals(3, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("What", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("?", tokens.get(1).value());

        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals("!", tokens.get(2).value());
    }

    @Test
    void shouldHandleConsecutivePunctuationBeginning() {
        var tokens = model.tokenize("--Now!");

        assertEquals(4, tokens.size());

        assertEquals(TokenType.Punctuation, tokens.get(0).type());
        assertEquals("-", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("-", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("Now", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldAssignCorrectSequenceNumbers() {
        var tokens = model.tokenize("Hello, world!");

        assertEquals(4, tokens.size());

        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(i + 1, tokens.get(i).sequenceNumber(),
                "Token at position " + i + " should have sequence number " + (i + 1));
        }

        // Test with a more complex sentence
        tokens = model.tokenize("I'll see you when you've finished!");

        assertEquals(7, tokens.size());
        assertEquals(1, tokens.get(0).sequenceNumber()); // I'll
        assertEquals(2, tokens.get(1).sequenceNumber()); // see
        assertEquals(3, tokens.get(2).sequenceNumber()); // you
        assertEquals(4, tokens.get(3).sequenceNumber()); // when
        assertEquals(5, tokens.get(4).sequenceNumber()); // you've
        assertEquals(6, tokens.get(5).sequenceNumber()); // finished
        assertEquals(7, tokens.get(6).sequenceNumber()); // !
    }

    @Test
    void tokenizeShouldAssignSequentialNumbersWithPunctuation() {
        var tokens = model.tokenize("Hello! What's up?");

        assertEquals(5, tokens.size());
        assertEquals(1, tokens.get(0).sequenceNumber()); // Hello
        assertEquals(2, tokens.get(1).sequenceNumber()); // !
        assertEquals(3, tokens.get(2).sequenceNumber()); // What's
        assertEquals(4, tokens.get(3).sequenceNumber()); // up
        assertEquals(5, tokens.get(4).sequenceNumber()); // ?
    }

    @Test
    void combineTokensShouldHandleEmptyList() {
        assertEquals("", model.combineTokens(List.of()));
    }

    @Test
    void combineTokensShouldHandleSimpleSentence() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Hello", 1),
            new Token(TokenType.Punctuation, ",", 2),
            new Token(TokenType.Word, "world", 3),
            new Token(TokenType.Punctuation, "!", 4)
        );
        assertEquals("Hello, world!", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleNumbersAndPunctuation() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "I", 1),
            new Token(TokenType.Word, "have", 2),
            new Token(TokenType.Number, "42", 3),
            new Token(TokenType.Word, "apples", 4),
            new Token(TokenType.Punctuation, ".", 5)
        );
        assertEquals("I have 42 apples.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleContractionsAndApostrophes() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "I'm", 1),
            new Token(TokenType.Word, "don't", 2),
            new Token(TokenType.Word, "know", 3),
            new Token(TokenType.Punctuation, ".", 4)
        );
        assertEquals("I'm don't know.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleMultipleSentences() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Hello", 1),
            new Token(TokenType.Punctuation, ".", 2),
            new Token(TokenType.Word, "How", 3),
            new Token(TokenType.Word, "are", 4),
            new Token(TokenType.Word, "you", 5),
            new Token(TokenType.Punctuation, "?", 6),
            new Token(TokenType.Word, "I", 7),
            new Token(TokenType.Word, "am", 8),
            new Token(TokenType.Word, "fine", 9),
            new Token(TokenType.Punctuation, "!", 10)
        );
        assertEquals("Hello. How are you? I am fine!", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleNestedPunctuation() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "He", 1),
            new Token(TokenType.Word, "said", 2),
            new Token(TokenType.Punctuation, ",", 3),
            new Token(TokenType.Punctuation, "\"", 4),
            new Token(TokenType.Word, "Hello", 5),
            new Token(TokenType.Punctuation, ",", 6),
            new Token(TokenType.Word, "world", 7),
            new Token(TokenType.Punctuation, "!", 8),
            new Token(TokenType.Punctuation, "\"", 9)
        );
        assertEquals("He said, \"Hello, world!\"", model.combineTokens(tokens));
    }
}


