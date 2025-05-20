package com.munetmo.lingetic.LanguageTestService.Entities;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SentenceTest {
    @Test
    void shouldCreateSentenceWithValidParameters() {
        // Given
        UUID id = UUID.randomUUID();
        Language sourceLanguage = Language.English;
        String sourceText = "Hello, how are you?";
        Language translationLanguage = Language.French;
        String translationText = "Bonjour, comment ça va ?";

        // When
        Sentence sentence = new Sentence(id, sourceLanguage, sourceText, translationLanguage, translationText);

        // Then
        assertNotNull(sentence);
        assertEquals(id, sentence.id());
        assertEquals(sourceLanguage, sentence.sourceLanguage());
        assertEquals(sourceText, sentence.sourceText());
        assertEquals(translationLanguage, sentence.translationLanguage());
        assertEquals(translationText, sentence.translationText());
    }

    @Test
    void shouldCreateSentenceUsingFactoryMethod() {
        // Given
        Language sourceLanguage = Language.English;
        String sourceText = "Hello, how are you?";
        Language translationLanguage = Language.French;
        String translationText = "Bonjour, comment ça va ?";

        // When
        Sentence sentence = Sentence.create(sourceLanguage, sourceText, translationLanguage, translationText);

        // Then
        assertNotNull(sentence);
        assertNotNull(sentence.id());
        assertEquals(sourceLanguage, sentence.sourceLanguage());
        assertEquals(sourceText, sentence.sourceText());
        assertEquals(translationLanguage, sentence.translationLanguage());
        assertEquals(translationText, sentence.translationText());
    }

    @Test
    void shouldThrowExceptionForBlankSourceText() {
        // Given
        String expectedMessage = "Source text cannot be blank";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Sentence(
                UUID.randomUUID(),
                Language.English,
                " ",
                Language.French,
                "Test"
            )
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForBlankTranslationText() {
        // Given
        String expectedMessage = "Translation text cannot be blank";

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Sentence(
                UUID.randomUUID(),
                Language.English,
                "Test",
                Language.French,
                " "
            )
        );
        assertEquals(expectedMessage, exception.getMessage());
    }
}
