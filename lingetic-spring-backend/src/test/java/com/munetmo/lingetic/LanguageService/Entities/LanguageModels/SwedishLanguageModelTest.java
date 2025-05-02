package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwedishLanguageModelTest {
    private SwedishLanguageModel model;

    @BeforeEach
    void setUp() {
        model = new SwedishLanguageModel();
    }

    @Test
    void getLanguageShouldReturnSwedish() {
        assertEquals(Language.Swedish, model.getLanguage());
    }

    @Test
    void areEquivalentShouldMatchIdenticalStrings() {
        assertTrue(model.areEquivalent("hej", "hej"));
        assertFalse(model.areEquivalent("hej", "hejsan"));
    }

    @Test
    void areEquivalentShouldIgnoreCase() {
        assertTrue(model.areEquivalent("Hej", "hej"));
        assertTrue(model.areEquivalent("HEJ", "hej"));
        assertTrue(model.areEquivalent("HeJ", "hEj"));
    }

    @Test
    void areEquivalentShouldIgnoreLeadingAndTrailingWhitespace() {
        assertTrue(model.areEquivalent("hej ", "hej"));
        assertTrue(model.areEquivalent(" hej", "hej "));
        assertTrue(model.areEquivalent("  hej  ", "hej"));
    }

    @Test
    void areEquivalentShouldWorkWithMultipleWords() {
        assertTrue(model.areEquivalent("hej   världen", "hej världen"));
        assertTrue(model.areEquivalent("hej\tvärlden", "hej världen"));
        assertTrue(model.areEquivalent("hej\nvärlden", "hej världen"));
        assertTrue(model.areEquivalent("Hej, världen!", "hej världen"));
        assertFalse(model.areEquivalent("hej världen", "hejdå världen!"));
    }

    @Test
    void areEquivalentShouldHandleEmptyStrings() {
        assertTrue(model.areEquivalent("", ""));
        assertTrue(model.areEquivalent(" ", " "));
        assertTrue(model.areEquivalent(" ", ""));
    }

    @Test
    void areEquivalentShouldIgnoreLeadingTrailingAndIntraWordPunctuation() {
        assertTrue(model.areEquivalent("hej!", "hej!"));
        assertTrue(model.areEquivalent("hej!", "hej"));
        assertTrue(model.areEquivalent("hej, världen", "hej världen"));
        assertFalse(model.areEquivalent("Jag är", "Jag ar")); // ä ≠ a
    }

    @Test
    void areEquivalentShouldHandleNumbers() {
        assertTrue(model.areEquivalent("1234", "  1234 "));
    }

    @Test
    void areEquivalentShouldPreserveAccentsAndSwedishLetters() {
        assertFalse(model.areEquivalent("får", "far"));
        assertFalse(model.areEquivalent("får", "for"));
        assertTrue(model.areEquivalent("får", "FÅR"));
        assertTrue(model.areEquivalent("får", "får"));
        assertFalse(model.areEquivalent("Jag älskar dig", "Jag alskar dig")); // ä ≠ a
        assertFalse(model.areEquivalent("Jag får", "Jag far")); // å ≠ a
        assertTrue(model.areEquivalent("Jag får", "JAG FÅR")); // case-insensitive
    }

    @Test
    void areEquivalentShouldHandleOnlyPunctuation() {
        assertTrue(model.areEquivalent("!!!", "!!!"));
        assertTrue(model.areEquivalent("!!!", "! ! !"));
        assertTrue(model.areEquivalent("! ! !", "!!!"));
    }

    @Test
    void areEquivalentShouldHandleOnlyNumbers() {
        assertTrue(model.areEquivalent("123", "123"));
        assertFalse(model.areEquivalent("123", "321"));
    }

    @Test
    void areEquivalentShouldHandleNumbersAndPunctuation() {
        assertTrue(model.areEquivalent("Jag har 1, 2 och 3.", "jag har 1 2 och 3"));
        assertFalse(model.areEquivalent("Jag har 1 äpple", "Jag har 2 äpplen"));
    }

    @Test
    void tokenizeShouldTokenizeSimpleSentences() {
        var tokens = model.tokenize("Hej, världen!");
        assertEquals(4, tokens.size());
        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Hej", tokens.get(0).value());
        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals(",", tokens.get(1).value());
        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("världen", tokens.get(2).value());
        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldTokenizeWithSwedishCharacters() {
        var tokens = model.tokenize("Får äter hö!");
        assertEquals(4, tokens.size());
        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Får", tokens.get(0).value());
        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("äter", tokens.get(1).value());
        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("hö", tokens.get(2).value());
        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void testTokenizeWithNumbersAndPunctuation() {
        List<Token> tokens = model.tokenize("Jag har 2 äpplen, 1 päron.");
        assertEquals(8, tokens.size());
        assertEquals("Jag", tokens.get(0).value());
        assertEquals("har", tokens.get(1).value());
        assertEquals("2", tokens.get(2).value());
        assertEquals("äpplen", tokens.get(3).value());
        assertEquals(",", tokens.get(4).value());
        assertEquals("1", tokens.get(5).value());
        assertEquals("päron", tokens.get(6).value());
        assertEquals(".", tokens.get(7).value());
    }

    @Test
    void testTokenizeWithOnlyPunctuation() {
        List<Token> tokens = model.tokenize("!!!");
        assertEquals(3, tokens.size());
        for (Token t : tokens) {
            assertEquals("!", t.value());
        }
    }

    @Test
    void testTokenizeWithEmptyAndBlank() {
        assertTrue(model.tokenize("").isEmpty());
        assertTrue(model.tokenize("   ").isEmpty());
    }

    @Test
    void testTokenizeWithTabAndNewline() {
        List<Token> tokens = model.tokenize("Hej\tdu\nvärlden!");
        assertEquals(4, tokens.size());
        assertEquals("Hej", tokens.get(0).value());
        assertEquals("du", tokens.get(1).value());
        assertEquals("världen", tokens.get(2).value());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void testEquivalentWithSwedishAndEnglishLetters() {
        assertFalse(model.areEquivalent("Jag älskar dig", "Jag alskar dig")); // ä ≠ a
        assertFalse(model.areEquivalent("Jag får", "Jag far")); // å ≠ a
        assertTrue(model.areEquivalent("Jag får", "JAG FÅR")); // case-insensitive
    }

    @Test
    void testEquivalentWithNumbersAndPunctuation() {
        assertTrue(model.areEquivalent("Jag har 1, 2 och 3.", "jag har 1 2 och 3"));
        assertFalse(model.areEquivalent("Jag har 1 äpple", "Jag har 2 äpplen"));
    }

    @Test
    void testEquivalentWithOnlyPunctuation() {
        assertTrue(model.areEquivalent("!!!", "!!!"));
        assertTrue(model.areEquivalent("!!!", "! ! !"));
        assertTrue(model.areEquivalent("! ! !", "!!!"));
    }

    @Test
    void testEquivalentWithOnlyNumbers() {
        assertTrue(model.areEquivalent("123", "123"));
        assertFalse(model.areEquivalent("123", "321"));
    }
}
