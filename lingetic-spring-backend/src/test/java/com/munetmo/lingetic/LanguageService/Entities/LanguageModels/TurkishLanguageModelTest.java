package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.TurkishLanguageModel;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}