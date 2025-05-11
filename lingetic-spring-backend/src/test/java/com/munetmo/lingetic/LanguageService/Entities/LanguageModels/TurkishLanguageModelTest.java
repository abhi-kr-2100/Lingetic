package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class TurkishLanguageModelTest {
    private TurkishLanguageModel model;

    @BeforeEach
    void setUp() {
        model = new TurkishLanguageModel();
    }

    @Test
    void getLanguageShouldReturnTurkish() {
        assertEquals(Language.Turkish, model.getLanguage());
    }

    @Test
    void areEquivalentShouldMatchIdenticalStrings() {
        assertTrue(model.areEquivalent("merhaba", "merhaba"));
        assertFalse(model.areEquivalent("merhaba", "dünya"));
    }

    @Test
    void areEquivalentShouldHandleTurkishSpecificCases() {
        assertTrue(model.areEquivalent("Işık", "ışık"));
        assertTrue(model.areEquivalent("İstanbul", "istanbul"));
        assertTrue(model.areEquivalent("IŞIK", "ışık"));
    }

    @Test
    void areEquivalentShouldHandleTurkishCharacters() {
        assertTrue(model.areEquivalent("güneş", "güneş"));
        assertTrue(model.areEquivalent("çiçek", "çiçek"));
        assertTrue(model.areEquivalent("öğle", "öğle"));
        assertTrue(model.areEquivalent("şeker", "şeker"));
    }

    @Test
    void areEquivalentShouldIgnoreLeadingAndTrailingWhitespace() {
        assertTrue(model.areEquivalent("merhaba ", "merhaba"));
        assertTrue(model.areEquivalent(" merhaba", "merhaba "));
        assertTrue(model.areEquivalent("  merhaba  ", "merhaba"));
    }

    @Test
    void areEquivalentShouldWorkWithMultipleWords() {
        assertTrue(model.areEquivalent("merhaba   dünya", "merhaba dünya"));
        assertTrue(model.areEquivalent("merhaba\tdünya", "merhaba dünya"));
        assertTrue(model.areEquivalent("merhaba\ndünya", "merhaba dünya"));
        assertTrue(model.areEquivalent("Merhaba, dünya!", "merhaba dünya"));
    }

    @Test
    void areEquivalentShouldHandleEmptyStrings() {
        assertTrue(model.areEquivalent("", ""));
        assertTrue(model.areEquivalent(" ", " "));
        assertTrue(model.areEquivalent(" ", ""));
    }

    @Test
    void areEquivalentShouldIgnoreLeadingTrailingAndIntraWordPunctuation() {
        assertTrue(model.areEquivalent("merhaba!", "merhaba"));
        assertTrue(model.areEquivalent("güzel,", "güzel"));
        assertTrue(model.areEquivalent("nasılsın?", "nasılsın"));
    }

    @Test
    void areEquivalentShouldHandleNumbers() {
        assertTrue(model.areEquivalent("1234", "1234"));
        assertTrue(model.areEquivalent("  1234 ", "1234"));
    }

    @Test
    void areEquivalentShouldHandleApostrophes() {
        assertFalse(model.areEquivalent("İstanbul'da", "istanbulda"));
        assertFalse(model.areEquivalent("Ali'nin", "alinin"));
    }

    @Test
    void tokenizeShouldTokenizeSimpleSentences() {
        var tokens = model.tokenize("Merhaba, dünya!");

        assertEquals(4, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Merhaba", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals(",", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("dünya", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldKeepPunctuationBetweenLettersAsOneWord() {
        var tokens = model.tokenize("Ben'im kitabım ve Ali'nin kalemi.");

        assertEquals(6, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Ben'im", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("kitabım", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("ve", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("Ali'nin", tokens.get(3).value());

        assertEquals(TokenType.Word, tokens.get(4).type());
        assertEquals("kalemi", tokens.get(4).value());

        assertEquals(TokenType.Punctuation, tokens.get(5).type());
        assertEquals(".", tokens.get(5).value());
    }

    @Test
    void tokenizeShouldTokenizeNumbers() {
        var tokens = model.tokenize("Ben 10 yaşındayım.");
        assertEquals(4, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Ben", tokens.get(0).value());

        assertEquals(TokenType.Number, tokens.get(1).type());
        assertEquals("10", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("yaşındayım", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals(".", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldTreatNumbersWithPunctuationAsNumbers() {
        var tokens = model.tokenize("1+2 4.500 3,14 4#2");
        assertEquals(4, tokens.size());

        assertEquals(new Token(TokenType.Number, "1+2", 0), tokens.get(0));
        assertEquals(new Token(TokenType.Number, "4.500", 4), tokens.get(1));
        assertEquals(new Token(TokenType.Number, "3,14", 10), tokens.get(2));
        assertEquals(new Token(TokenType.Number, "4#2", 15), tokens.get(3));
    }

    @Test
    void tokenizeShouldTreatAMixOfNumbersAndLettersAsWords() {
        var tokens = model.tokenize("Oda 10A'da.");
        assertEquals(3, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Oda", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("10A'da", tokens.get(1).value());

        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals(".", tokens.get(2).value());
    }

    @Test
    void tokenizeShouldHandleIsolatedPunctuations() {
        var tokens = model.tokenize("Bu bir sembol: .");

        assertEquals(5, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Bu", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("bir", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("sembol", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals(":", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals(".", tokens.get(4).value());
    }

    @Test
    void tokenizeShouldHandleTurkishQuotations() {
        var tokens = model.tokenize("O \"Merhaba\" dedi.");

        assertEquals(6, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("O", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("\"", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("Merhaba", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("\"", tokens.get(3).value());

        assertEquals(TokenType.Word, tokens.get(4).type());
        assertEquals("dedi", tokens.get(4).value());

        assertEquals(TokenType.Punctuation, tokens.get(5).type());
        assertEquals(".", tokens.get(5).value());
    }

    @Test
    void shouldHandleConsecutivePunctuationEnding() {
        var tokens = model.tokenize("Ne?!");

        assertEquals(3, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Ne", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("?", tokens.get(1).value());

        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals("!", tokens.get(2).value());
    }

    @Test
    void shouldHandleConsecutivePunctuationBeginning() {
        var tokens = model.tokenize("--Şimdi!");

        assertEquals(4, tokens.size());

        assertEquals(TokenType.Punctuation, tokens.get(0).type());
        assertEquals("-", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("-", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("Şimdi", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldAssignCorrectStartIndexes() {
        var tokens = model.tokenize("Merhaba, dünya!");

        assertEquals(4, tokens.size());
        assertEquals(0, tokens.get(0).startIndex()); // Merhaba
        assertEquals(7, tokens.get(1).startIndex()); // ,
        assertEquals(9, tokens.get(2).startIndex()); // dünya
        assertEquals(14, tokens.get(3).startIndex()); // !
    }

    @Test
    void tokenizeShouldAssignCorrectStartIndexesWithExtraSpacings() {
        var tokens = model.tokenize("   Merhaba,    dünya  !   ");

        assertEquals(4, tokens.size());
        assertEquals(3, tokens.get(0).startIndex()); // Merhaba
        assertEquals(10, tokens.get(1).startIndex()); // ,
        assertEquals(15, tokens.get(2).startIndex()); // dünya
        assertEquals(22, tokens.get(3).startIndex()); // !
    }

    @Test
    void combineTokensShouldHandleEmptyList() {
        assertEquals("", model.combineTokens(List.of()));
    }

    @Test
    void combineTokensShouldHandleSimpleSentence() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Merhaba", 1),
            new Token(TokenType.Punctuation, ",", 2),
            new Token(TokenType.Word, "dünya", 3),
            new Token(TokenType.Punctuation, "!", 4)
        );
        assertEquals("Merhaba, dünya!", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleApostrophes() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Ben'im", 1),
            new Token(TokenType.Word, "kitabım", 2),
            new Token(TokenType.Word, "ve", 3),
            new Token(TokenType.Word, "Ali'nin", 4),
            new Token(TokenType.Word, "kalemi", 5),
            new Token(TokenType.Punctuation, ".", 6)
        );
        assertEquals("Ben'im kitabım ve Ali'nin kalemi.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleNumbersAndTurkishCharacters() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Ben", 1),
            new Token(TokenType.Number, "10", 2),
            new Token(TokenType.Word, "yaşındayım", 3),
            new Token(TokenType.Punctuation, ".", 4)
        );
        assertEquals("Ben 10 yaşındayım.", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleMultipleSentences() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Merhaba", 1),
            new Token(TokenType.Punctuation, ".", 2),
            new Token(TokenType.Word, "Nasıl", 3),
            new Token(TokenType.Word, "hisset", 4),
            new Token(TokenType.Word, "iyorson", 5),
            new Token(TokenType.Punctuation, "?", 6),
            new Token(TokenType.Word, "Ben", 7),
            new Token(TokenType.Word, "iyiyim", 8),
            new Token(TokenType.Punctuation, "!", 9)
        );
        assertEquals("Merhaba. Nasıl hisset iyorson? Ben iyiyim!", model.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleNestedPunctuation() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "O", 1),
            new Token(TokenType.Word, "dedi", 2),
            new Token(TokenType.Punctuation, ",", 3),
            new Token(TokenType.Punctuation, "\"", 4),
            new Token(TokenType.Word, "Merhaba", 5),
            new Token(TokenType.Punctuation, ",", 6),
            new Token(TokenType.Word, "dünya", 7),
            new Token(TokenType.Punctuation, "!", 8),
            new Token(TokenType.Punctuation, "\"", 9)
        );
        assertEquals("O dedi, \"Merhaba, dünya!\"", model.combineTokens(tokens));
    }
}
