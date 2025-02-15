package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}