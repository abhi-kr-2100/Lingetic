package com.munetmo.lingetic.LanguageTestService.Entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordExplanationTest {

    @Test
    void shouldCreateWordExplanationWithValidParameters() {
        // Given
        int startIndex = 0;
        String word = "test";
        List<String> properties = List.of("noun", "singular");
        String comment = "test comment";

        // When
        WordExplanation wordExplanation = new WordExplanation(startIndex, word, properties, comment);

        // Then
        assertNotNull(wordExplanation);
        assertEquals(startIndex, wordExplanation.startIndex());
        assertEquals(word, wordExplanation.word());
        assertEquals(properties, wordExplanation.properties());
        assertEquals(comment, wordExplanation.comment());
    }

    @Test
    void shouldThrowExceptionForNegativeStartIndex() {
        // Given
        int startIndex = -1;
        String word = "test";
        List<String> properties = List.of("noun");
        String comment = "comment";
        String expectedMessage = "startIndex must be non-negative";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WordExplanation(startIndex, word, properties, comment)
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForBlankWord() {
        // Given
        int startIndex = 0;
        String word = " ";
        List<String> properties = List.of("noun");
        String comment = "comment";
        String expectedMessage = "Word cannot be blank";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WordExplanation(startIndex, word, properties, comment)
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForBlankProperty() {
        // Given
        int startIndex = 0;
        String word = "test";
        List<String> properties = List.of("noun", " ");
        String comment = "comment";
        String expectedMessage = "Properties cannot contain blank strings";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WordExplanation(startIndex, word, properties, comment)
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForBlankComment() {
        // Given
        int startIndex = 0;
        String word = "test";
        List<String> properties = List.of("noun");
        String comment = " ";
        String expectedMessage = "Comment cannot be blank";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WordExplanation(startIndex, word, properties, comment)
        );
        assertEquals(expectedMessage, exception.getMessage());
    }
}
