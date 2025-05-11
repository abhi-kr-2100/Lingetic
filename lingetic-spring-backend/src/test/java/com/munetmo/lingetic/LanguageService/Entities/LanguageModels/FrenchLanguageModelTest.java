package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FrenchLanguageModelTest {

    private FrenchLanguageModel frenchLanguageModel;

    @BeforeEach
    void setUp() {
        frenchLanguageModel = new FrenchLanguageModel();
    }

    @Test
    void testGetLanguage() {
        assertEquals(Language.French, frenchLanguageModel.getLanguage());
    }

    @Test
    void testEquivalentSimpleStrings() {
        assertTrue(frenchLanguageModel.areEquivalent("bonjour", "bonjour"));
        assertTrue(frenchLanguageModel.areEquivalent("bonjour", "Bonjour"));
        assertTrue(frenchLanguageModel.areEquivalent("BONJOUR", "bonjour"));
        assertTrue(frenchLanguageModel.areEquivalent("bonjour", "BONJOUR"));
    }

    @Test
    void testEquivalentWithTrimming() {
        assertTrue(frenchLanguageModel.areEquivalent("bonjour", " bonjour"));
        assertTrue(frenchLanguageModel.areEquivalent("bonjour", "bonjour "));
        assertTrue(frenchLanguageModel.areEquivalent("bonjour", " bonjour "));
        assertTrue(frenchLanguageModel.areEquivalent(" bonjour ", "bonjour"));
    }

    @Test
    void testEquivalentWithPunctuation() {
        assertTrue(frenchLanguageModel.areEquivalent("bonjour!", "bonjour"));
        assertTrue(frenchLanguageModel.areEquivalent("!bonjour", "bonjour"));
        assertTrue(frenchLanguageModel.areEquivalent("!bonjour!", "bonjour"));
        assertTrue(frenchLanguageModel.areEquivalent("bonjour", "!bonjour!"));
    }

    @Test
    void testEquivalentWithMultipleSpaces() {
        assertTrue(frenchLanguageModel.areEquivalent("bonjour  monde", "bonjour monde"));
        assertTrue(frenchLanguageModel.areEquivalent("bonjour   monde", "bonjour monde"));
    }

    @Test
    void testPreservationOfAccents() {
        // These should NOT be equivalent because accents change meaning
        assertFalse(frenchLanguageModel.areEquivalent("café", "cafe"));
        assertFalse(frenchLanguageModel.areEquivalent("élève", "eleve"));
        assertFalse(frenchLanguageModel.areEquivalent("être", "etre"));
        assertFalse(frenchLanguageModel.areEquivalent("voilà", "voila"));
        assertFalse(frenchLanguageModel.areEquivalent("français", "francais"));

        // These should be equivalent (same word with same accents)
        assertTrue(frenchLanguageModel.areEquivalent("café", "café"));
        assertTrue(frenchLanguageModel.areEquivalent("élève", "élève"));
        assertTrue(frenchLanguageModel.areEquivalent("être", "être"));
    }

    @Test
    void testCaseInsensitivityWithAccents() {
        // Case insensitivity should still apply, but accents must be preserved
        assertTrue(frenchLanguageModel.areEquivalent("café", "Café"));
        assertTrue(frenchLanguageModel.areEquivalent("Élève", "élève"));
        assertTrue(frenchLanguageModel.areEquivalent("ÊTRE", "être"));

        // Different accents should not be equivalent
        assertFalse(frenchLanguageModel.areEquivalent("élève", "éléve"));
        assertFalse(frenchLanguageModel.areEquivalent("déjà", "dejà"));
    }

    @Test
    void testEquivalentWithPreservingFrenchSpecificCharacters() {
        // These should be equivalent only when the same characters are used (but
        // case-insensitive)
        assertTrue(frenchLanguageModel.areEquivalent("ça va", "ÇA VA"));
        assertFalse(frenchLanguageModel.areEquivalent("l'hôtel", "l'hotel"));
        assertTrue(frenchLanguageModel.areEquivalent("œuvre", "ŒUVRE"));
        assertFalse(frenchLanguageModel.areEquivalent("œuvre", "oeuvre"));
    }

    @Test
    void testNonEquivalentStrings() {
        assertFalse(frenchLanguageModel.areEquivalent("bonjour", "bonsoir"));
        assertFalse(frenchLanguageModel.areEquivalent("café", "caffe"));
        assertFalse(frenchLanguageModel.areEquivalent("être", "etree"));
    }

    @Test
    void testEquivalentWithMultipleWords() {
        assertTrue(frenchLanguageModel.areEquivalent("bonjour le monde", "Bonjour Le Monde"));
        assertFalse(frenchLanguageModel.areEquivalent("Comment ça va?", "comment ca va"));
        assertTrue(frenchLanguageModel.areEquivalent("Je m'appelle Pierre.", "je m'appelle pierre"));
    }

    @Test
    void testEquivalentWithEmptyAndBlankStrings() {
        assertTrue(frenchLanguageModel.areEquivalent("", ""));
        assertTrue(frenchLanguageModel.areEquivalent(" ", ""));
        assertTrue(frenchLanguageModel.areEquivalent("  ", ""));
        assertTrue(frenchLanguageModel.areEquivalent(" ", "  "));
    }

    @Test
    void testEquivalentWithNumbers() {
        assertFalse(frenchLanguageModel.areEquivalent("numéro 1", "numero 1"));
        assertTrue(frenchLanguageModel.areEquivalent("numéro 1", "Numéro 1"));
        assertTrue(frenchLanguageModel.areEquivalent("2 café", "2 Café"));
        assertFalse(frenchLanguageModel.areEquivalent("le 3ème étage", "le 3eme etage"));
    }

    @Test
    void testEquivalentWithMixedCaseAndAccents() {
        assertFalse(frenchLanguageModel.areEquivalent("ÉlÈvE", "eleve"));
        assertTrue(frenchLanguageModel.areEquivalent("ÉlÈvE", "élève"));
        assertTrue(frenchLanguageModel.areEquivalent("CaFé", "café"));
        assertFalse(frenchLanguageModel.areEquivalent("À BIENTÔT", "a bientot"));
        assertTrue(frenchLanguageModel.areEquivalent("À BIENTÔT", "à bientôt"));
    }

    @Test
    void testAccentsDifferentiateWords() {
        // Words where accents change meaning
        assertFalse(frenchLanguageModel.areEquivalent("ou", "où")); // "or" vs "where"
        assertFalse(frenchLanguageModel.areEquivalent("a", "à")); // "has" vs "to/at"
        assertFalse(frenchLanguageModel.areEquivalent("du", "dû")); // "of the" vs "due"
        assertFalse(frenchLanguageModel.areEquivalent("sur", "sûr")); // "on" vs "sure"
        assertFalse(frenchLanguageModel.areEquivalent("la", "là")); // "the" vs "there"
    }


    @Test
    void tokenizeShouldTokenizeSimpleSentences() {
        var tokens = frenchLanguageModel.tokenize("Bonjour, monde!");

        assertEquals(4, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Bonjour", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals(",", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("monde", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldKeepPunctuationBetweenLettersAsOneWord() {
        var tokens = frenchLanguageModel.tokenize("J'irai quand vous l'aurez terminé, et je suis d'accord.");

        assertEquals(11, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("J'irai", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("quand", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("vous", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("l'aurez", tokens.get(3).value());

        assertEquals(TokenType.Word, tokens.get(4).type());
        assertEquals("terminé", tokens.get(4).value());

        assertEquals(TokenType.Punctuation, tokens.get(5).type());
        assertEquals(",", tokens.get(5).value());

        assertEquals(TokenType.Word, tokens.get(6).type());
        assertEquals("et", tokens.get(6).value());

        assertEquals(TokenType.Word, tokens.get(7).type());
        assertEquals("je", tokens.get(7).value());

        assertEquals(TokenType.Word, tokens.get(8).type());
        assertEquals("suis", tokens.get(8).value());

        assertEquals(TokenType.Word, tokens.get(9).type());
        assertEquals("d'accord", tokens.get(9).value());

        assertEquals(TokenType.Punctuation, tokens.get(10).type());
        assertEquals(".", tokens.get(10).value());
    }

    @Test
    void tokenizeShouldTokenizeNumbers() {
        var tokens = frenchLanguageModel.tokenize("J'ai 10 ans.");
        assertEquals(4, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("J'ai", tokens.get(0).value());

        assertEquals(TokenType.Number, tokens.get(1).type());
        assertEquals("10", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("ans", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals(".", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldTreatNumbersWithPunctuationAsNumbers() {
        var tokens = frenchLanguageModel.tokenize("1+2 4,500 3.14 4#2");
        assertEquals(4, tokens.size());

        assertEquals(new Token(TokenType.Number, "1+2", 0), tokens.get(0));
        assertEquals(new Token(TokenType.Number, "4,500", 4), tokens.get(1));
        assertEquals(new Token(TokenType.Number, "3.14", 10), tokens.get(2));
        assertEquals(new Token(TokenType.Number, "4#2", 15), tokens.get(3));
    }

    @Test
    void tokenizeShouldTreatAMixOfNumbersAndLettersAsWords() {
        var tokens = frenchLanguageModel.tokenize("Je suis dans l'appartement 10A.");
        assertEquals(6, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Je", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("suis", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("dans", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("l'appartement", tokens.get(3).value());

        assertEquals(TokenType.Word, tokens.get(4).type());
        assertEquals("10A", tokens.get(4).value());

        assertEquals(TokenType.Punctuation, tokens.get(5).type());
        assertEquals(".", tokens.get(5).value());
    }

    @Test
    void tokenizeShouldHandleFrenchGuillemets() {
        var tokens = frenchLanguageModel.tokenize("« Bonjour le monde ! »");

        assertEquals(6, tokens.size());

        assertEquals(TokenType.Punctuation, tokens.get(0).type());
        assertEquals("«", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("Bonjour", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("le", tokens.get(2).value());

        assertEquals(TokenType.Word, tokens.get(3).type());
        assertEquals("monde", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals("!", tokens.get(4).value());

        assertEquals(TokenType.Punctuation, tokens.get(5).type());
        assertEquals("»", tokens.get(5).value());
    }

    @Test
    void tokenizeShouldHandleNestedQuotations() {
        var tokens = frenchLanguageModel.tokenize("Elle a dit : « Il m'a répondu \"Je ne sais pas\" hier. »");

        assertEquals(17, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Elle", tokens.get(0).value());

        assertEquals(TokenType.Word, tokens.get(1).type());
        assertEquals("a", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("dit", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals(":", tokens.get(3).value());

        assertEquals(TokenType.Punctuation, tokens.get(4).type());
        assertEquals("«", tokens.get(4).value());

        assertEquals(TokenType.Word, tokens.get(5).type());
        assertEquals("Il", tokens.get(5).value());

        assertEquals(TokenType.Word, tokens.get(6).type());
        assertEquals("m'a", tokens.get(6).value());

        assertEquals(TokenType.Word, tokens.get(7).type());
        assertEquals("répondu", tokens.get(7).value());

        assertEquals(TokenType.Punctuation, tokens.get(8).type());
        assertEquals("\"", tokens.get(8).value());

        assertEquals(TokenType.Word, tokens.get(9).type());
        assertEquals("Je", tokens.get(9).value());

        assertEquals(TokenType.Word, tokens.get(10).type());
        assertEquals("ne", tokens.get(10).value());

        assertEquals(TokenType.Word, tokens.get(11).type());
        assertEquals("sais", tokens.get(11).value());

        assertEquals(TokenType.Word, tokens.get(12).type());
        assertEquals("pas", tokens.get(12).value());

        assertEquals(TokenType.Punctuation, tokens.get(13).type());
        assertEquals("\"", tokens.get(13).value());

        assertEquals(TokenType.Word, tokens.get(14).type());
        assertEquals("hier", tokens.get(14).value());

        assertEquals(TokenType.Punctuation, tokens.get(15).type());
        assertEquals(".", tokens.get(15).value());

        assertEquals(TokenType.Punctuation, tokens.get(16).type());
        assertEquals("»", tokens.get(16).value());
    }

    @Test
    void shouldHandleConsecutivePunctuationEnding() {
        var tokens = frenchLanguageModel.tokenize("Quoi?!");

        assertEquals(3, tokens.size());

        assertEquals(TokenType.Word, tokens.get(0).type());
        assertEquals("Quoi", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("?", tokens.get(1).value());

        assertEquals(TokenType.Punctuation, tokens.get(2).type());
        assertEquals("!", tokens.get(2).value());
    }

    @Test
    void shouldHandleConsecutivePunctuationBeginning() {
        var tokens = frenchLanguageModel.tokenize("--Maintenant!");

        assertEquals(4, tokens.size());

        assertEquals(TokenType.Punctuation, tokens.get(0).type());
        assertEquals("-", tokens.get(0).value());

        assertEquals(TokenType.Punctuation, tokens.get(1).type());
        assertEquals("-", tokens.get(1).value());

        assertEquals(TokenType.Word, tokens.get(2).type());
        assertEquals("Maintenant", tokens.get(2).value());

        assertEquals(TokenType.Punctuation, tokens.get(3).type());
        assertEquals("!", tokens.get(3).value());
    }

    @Test
    void tokenizeShouldAssignCorrectStartIndexes() {
        var tokens = frenchLanguageModel.tokenize("Bonjour, monde!");

        assertEquals(4, tokens.size());
        assertEquals(0, tokens.get(0).startIndex()); // Bonjour
        assertEquals(7, tokens.get(1).startIndex()); // ,
        assertEquals(9, tokens.get(2).startIndex()); // monde
        assertEquals(14, tokens.get(3).startIndex()); // !
    }

    @Test
    void tokenizeShouldAssignCorrectStartIndexesWithExtraSpacings() {
        var tokens = frenchLanguageModel.tokenize("   Bonjour,    monde  !   ");

        assertEquals(4, tokens.size());
        assertEquals(3, tokens.get(0).startIndex()); // Bonjour
        assertEquals(10, tokens.get(1).startIndex()); // ,
        assertEquals(15, tokens.get(2).startIndex()); // monde
        assertEquals(22, tokens.get(3).startIndex()); // !
    }

    @Test
    void combineTokensShouldHandleEmptyList() {
        assertEquals("", frenchLanguageModel.combineTokens(List.of()));
    }

    @Test
    void combineTokensShouldHandleSimpleSentence() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Bonjour", 1),
            new Token(TokenType.Punctuation, ",", 2),
            new Token(TokenType.Word, "monde", 3),
            new Token(TokenType.Punctuation, "!", 4)
        );
        assertEquals("Bonjour, monde!", frenchLanguageModel.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleContractionsAndQuotations() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "J'aime", 1),
            new Token(TokenType.Word, "l'école", 2),
            new Token(TokenType.Punctuation, "«", 3),
            new Token(TokenType.Word, "C'est", 4),
            new Token(TokenType.Word, "parfait", 5),
            new Token(TokenType.Punctuation, "»", 6)
        );
        assertEquals("J'aime l'école «C'est parfait»", frenchLanguageModel.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleNumbersAndAccents() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Il", 1),
            new Token(TokenType.Word, "y", 2),
            new Token(TokenType.Word, "a", 3),
            new Token(TokenType.Number, "42", 4),
            new Token(TokenType.Word, "élèves", 5),
            new Token(TokenType.Punctuation, ".", 6)
        );
        assertEquals("Il y a 42 élèves.", frenchLanguageModel.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleMultipleSentences() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Bonjour", 1),
            new Token(TokenType.Punctuation, ".", 2),
            new Token(TokenType.Word, "Comment", 3),
            new Token(TokenType.Word, "allez-vous", 4),
            new Token(TokenType.Punctuation, "?", 5),
            new Token(TokenType.Word, "Je", 6),
            new Token(TokenType.Word, "vais", 7),
            new Token(TokenType.Word, "bien", 8),
            new Token(TokenType.Punctuation, "!", 9)
        );
        assertEquals("Bonjour. Comment allez-vous? Je vais bien!", frenchLanguageModel.combineTokens(tokens));
    }

    @Test
    void combineTokensShouldHandleNestedPunctuation() {
        var tokens = Arrays.asList(
            new Token(TokenType.Word, "Il", 1),
            new Token(TokenType.Word, "dit", 2),
            new Token(TokenType.Punctuation, ",", 3),
            new Token(TokenType.Punctuation, "«", 4),
            new Token(TokenType.Word, "Bonjour", 5),
            new Token(TokenType.Punctuation, ",", 6),
            new Token(TokenType.Word, "monde", 7),
            new Token(TokenType.Punctuation, "!", 8),
            new Token(TokenType.Punctuation, "»", 9)
        );
        assertEquals("Il dit, «Bonjour, monde!»", frenchLanguageModel.combineTokens(tokens));
    }
}
