package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.FrenchLanguageModel;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
