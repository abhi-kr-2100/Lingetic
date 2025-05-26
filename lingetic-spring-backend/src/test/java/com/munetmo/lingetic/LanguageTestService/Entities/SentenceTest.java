
package com.munetmo.lingetic.LanguageTestService.Entities;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.Test;

import java.util.List;
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
        List<WordExplanation> wordExplanations = List.of(
            new WordExplanation(0, "Hello", List.of("greeting"), "A common greeting"),
            new WordExplanation(7, "how", List.of("question"), "Asking about manner")
        );

        // When
        Sentence sentence = new Sentence(id, sourceLanguage, sourceText, translationLanguage, translationText, 10, wordExplanations);

        // Then
        assertNotNull(sentence);
        assertEquals(id, sentence.id());
        assertEquals(sourceLanguage, sentence.sourceLanguage());
        assertEquals(sourceText, sentence.sourceText());
        assertEquals(translationLanguage, sentence.translationLanguage());
        assertEquals(translationText, sentence.translationText());
        assertEquals(wordExplanations, sentence.sourceWordExplanations());
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
                "Test",
                10,
                List.of()
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
                " ",
                10,
                List.of()
            )
        );
        assertEquals(expectedMessage, exception.getMessage());
    }
}
