package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JapaneseModifiedHepburnLanguageModelTest {
    private JapaneseModifiedHepburnLanguageModel model;

    @BeforeEach
    void setUp() {
        model = new JapaneseModifiedHepburnLanguageModel();
    }

    @Test
    void getLanguageShouldReturnJapaneseModifiedHepburn() {
        assertEquals(Language.JapaneseModifiedHepburn, model.getLanguage());
    }

    @Test
    void tokenizeShouldHandleEmptyStrings() {
        assertTrue(model.tokenize("").isEmpty());
        assertTrue(model.tokenize(" ").isEmpty());
    }

    @Test
    void areEquivalentShouldHandleEmptyStrings() {
        assertTrue(model.areEquivalent("", ""));
        assertTrue(model.areEquivalent(" ", " "));
        assertTrue(model.areEquivalent(" ", ""));
    }

    @Test
    void areEquivalentShouldMatchRomanizedForms() {
        assertTrue(model.areEquivalent("inu", "inu"));
    }

    @Test
    void areEquivalentShouldIgnorePunctuation() {
        assertTrue(model.areEquivalent("inu!", "inu"));
    }

    @Test
    void areEquivalentShouldWorkWithMacronA() {
        assertTrue(model.areEquivalent("kādo", "kādo")); // card
        assertFalse(model.areEquivalent("kādo", "kaado"));
        assertTrue(model.areEquivalent("KĀDO", "kādo")); // case insensitive
        assertFalse(model.areEquivalent("kado", "kādo")); // different meaning
    }

    @Test
    void areEquivalentShouldWorkWithMacronI() {
        assertTrue(model.areEquivalent("onīsan", "onīsan")); // older brother
        assertFalse(model.areEquivalent("onīsan", "oniisan"));
        assertTrue(model.areEquivalent("ONĪSAN", "onīsan")); // case insensitive
        assertFalse(model.areEquivalent("onisan", "onīsan")); // different meaning
    }

    @Test
    void areEquivalentShouldWorkWithMacronU() {
        assertTrue(model.areEquivalent("yūki", "yūki")); // courage
        assertFalse(model.areEquivalent("yūki", "yuuki"));
        assertTrue(model.areEquivalent("YŪKI", "yūki")); // case insensitive
        assertFalse(model.areEquivalent("yuki", "yūki")); // different meaning (snow vs courage)
    }

    @Test
    void areEquivalentShouldWorkWithMacronE() {
        assertTrue(model.areEquivalent("onēsan", "onēsan")); // older sister
        assertFalse(model.areEquivalent("onēsan", "oneesan"));
        assertTrue(model.areEquivalent("ONĒSAN", "onēsan")); // case insensitive
        assertFalse(model.areEquivalent("onesan", "onēsan")); // different meaning
    }

    @Test
    void areEquivalentShouldWorkWithMacronO() {
        assertTrue(model.areEquivalent("tōkyō", "tōkyō")); // Tokyo
        assertFalse(model.areEquivalent("tōkyō", "toukyou"));
        assertTrue(model.areEquivalent("TŌKYŌ", "tōkyō")); // case insensitive
        assertFalse(model.areEquivalent("tokyo", "tōkyō")); // incorrect romanization
    }

    @Test
    void areEquivalentShouldWorkWithMultipleMacrons() {
        assertTrue(model.areEquivalent("tōkyō", "tōkyō")); // Tokyo
        assertTrue(model.areEquivalent("kyōsō", "kyōsō")); // competition
        assertTrue(model.areEquivalent("shōchū", "shōchū")); // distilled spirits
        assertTrue(model.areEquivalent("kōkōsei", "kōkōsei")); // high school student
    }

    @Test
    void areEquivalentShouldHandleMacronsWithPunctuation() {
        assertTrue(model.areEquivalent("Tōkyō!", "tōkyō"));
        assertTrue(model.areEquivalent("Tōkyō?", "tōkyō"));
        assertTrue(model.areEquivalent("Tōkyō...", "tōkyō"));
    }

    @Test
    void areEquivalentShouldHandleMacronsWithSpaces() {
        assertTrue(model.areEquivalent("  tōkyō  ", "tōkyō"));
        assertFalse(model.areEquivalent("Tōkyō-to", "tōkyō to")); // Tokyo metropolis
        assertTrue(model.areEquivalent("Tōkyō\tto", "tōkyō to"));
    }

    @Test
    void tokenizeShouldHandleTokenTypes() {
        var tokens = model.tokenize("Watashi wa 25-sai desu.");

        assertEquals(5, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Watashi", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("wa", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("25-sai", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("desu", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals(".", tokens.get(4).value());
    }

    @Test
    void tokenizeShouldHandleMacronsInWords() {
        var tokens = model.tokenize("Tōkyō wa ōkii desu.");

        assertEquals(5, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Tōkyō", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("wa", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("ōkii", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("desu", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals(".", tokens.get(4).value());
    }

    @Test
    void tokenizeShouldHandleJapaneseQuotations() {
        var tokens = model.tokenize("Sensei wa \"Konnichiwa\" to iimashita.");

        assertEquals(8, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Sensei", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("wa", tokens.get(1).value());

        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals("\"", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("Konnichiwa", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals("\"", tokens.get(4).value());

        assertEquals(TokenType.Word, tokens.get(5).type());
        assertEquals("to", tokens.get(5).value());

        assertEquals(TokenType.Word, tokens.get(6).type());
        assertEquals("iimashita", tokens.get(6).value());

        assertEquals(TokenType.Punctuation, tokens.get(7).type());
        assertEquals(".", tokens.get(7).value());
    }

    @Test
    void tokenizeShouldHandleNumbers() {
        var tokens = model.tokenize("Kurasu ni 42-nin imasu.");
        assertEquals(5, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Kurasu", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("ni", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("42-nin", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("imasu", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals(".", tokens.get(4).value());
    }

    @Test
    void tokenizeShouldHandleComplexPunctuation() {
        var tokens = model.tokenize("Sugoi...! Hontō?!");

        assertEquals(8, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Sugoi", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals(".", tokens.get(1).value());

        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals(".", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals(".", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals("!", tokens.get(4).value());

        assertEquals(TokenType.Word, tokens.get(5).type());
        assertEquals("Hontō", tokens.get(5).value());

        assertEquals(TokenType.Punctuation, tokens.get(6).type());
        assertEquals("?", tokens.get(6).value());

        assertEquals(TokenType.Punctuation, tokens.get(7).type());
        assertEquals("!", tokens.get(7).value());
    }

    @Test
    void tokenizeShouldKeepNumbersAndLettersSeparate() {
        var tokens = model.tokenize("Room 101A wa doko desu ka?");

        assertEquals(7, tokens.size());
        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Room", tokens.get(0).value());
        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("101A", tokens.get(1).value());
        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("wa", tokens.get(2).value());
        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("doko", tokens.get(3).value());
        assertEquals(TokenType.Word, tokens.get(4).type());
        assertEquals("desu", tokens.get(4).value());
        assertEquals(TokenType.Word, tokens.get(5).type());
        assertEquals("ka", tokens.get(5).value());
        assertEquals(TokenType.Punctuation, tokens.get(6).type());
        assertEquals("?", tokens.get(6).value());
    }

    @Test
    void combineTokensShouldHandleEmptyList() {
        assertEquals("", model.combineTokens(List.of()));
    }

    @Test
    void combineTokensShouldHandleSimpleSentence() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Watashi", 1),
            new Token(TokenType.Word, "wa", 2),
            new Token(TokenType.Word, "gakusei", 3),
            new Token(TokenType.Word, "desu", 4),
            new Token(TokenType.Punctuation, ".", 5)
        );
        assertEquals("Watashi wa gakusei desu.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleMacrons() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Tōkyō", 1),
            new Token(TokenType.Word, "wa", 2),
            new Token(TokenType.Word, "ōkii", 3),
            new Token(TokenType.Word, "desu", 4),
            new Token(TokenType.Punctuation, ".", 5)
        );
        assertEquals("Tōkyō wa ōkii desu.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleQuotationsAndNumbers() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Sensei", 1),
            new Token(TokenType.Word, "wa", 2),
            new Token(TokenType.Punctuation, "\"", 3),
            new Token(TokenType.Number, "42", 4),
            new Token(TokenType.Word, "desu", 5),
            new Token(TokenType.Punctuation, "\"", 6),
            new Token(TokenType.Word, "to", 7),
            new Token(TokenType.Word, "iimashita", 8),
            new Token(TokenType.Punctuation, ".", 9)
        );
        assertEquals("Sensei wa \"42 desu\" to iimashita.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleMultipleSentences() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Ohayō", 1),
            new Token(TokenType.Punctuation, ".", 2),
            new Token(TokenType.Word, "Ogenki", 3),
            new Token(TokenType.Word, "desu", 4),
            new Token(TokenType.Word, "ka", 5),
            new Token(TokenType.Punctuation, "?", 6),
            new Token(TokenType.Word, "Hai", 7),
            new Token(TokenType.Punctuation, ",", 8),
            new Token(TokenType.Word, "genki", 9),
            new Token(TokenType.Word, "desu", 10),
            new Token(TokenType.Punctuation, ".", 11)
        );
        assertEquals("Ohayō. Ogenki desu ka? Hai, genki desu.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleNestedPunctuation() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Kare", 1),
            new Token(TokenType.Word, "wa", 2),
            new Token(TokenType.Word, "iimashita", 3),
            new Token(TokenType.Punctuation, ",", 4),
            new Token(TokenType.Punctuation, "\"", 5),
            new Token(TokenType.Word, "Konnichiwa", 6),
            new Token(TokenType.Punctuation, ",", 7),
            new Token(TokenType.Word, "sekai", 8),
            new Token(TokenType.Punctuation, "!", 9),
            new Token(TokenType.Punctuation, "\"", 10)
        );
        assertEquals("Kare wa iimashita, \"Konnichiwa, sekai!\"", model.combineTokens(tokens));
    }
}
