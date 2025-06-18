package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SourceToTargetTranslationAttemptResponseTest {
    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var response = new SourceToTargetTranslationAttemptResponse(AttemptStatus.Success, "test answer", List.of());

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("test answer", response.getCorrectAnswer());
        assertEquals(QuestionType.SourceToTargetTranslation, response.getQuestionType());
        assertEquals(Collections.emptyList(), response.getSourceWordExplanations());
    }

    @Test
    void constructorShouldCreateValidObjectWithWordExplanations() {
        var wordExplanations = List.of(
            new WordExplanation(0, "test", List.of("noun"), "A test word")
        );
        var response = new SourceToTargetTranslationAttemptResponse(AttemptStatus.Success, "test answer", wordExplanations);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("test answer", response.getCorrectAnswer());
        assertEquals(QuestionType.SourceToTargetTranslation, response.getQuestionType());
        assertEquals(wordExplanations, response.getSourceWordExplanations());
    }

    @Test
    void constructorShouldHandleEmptyWordExplanations() {
        var response = new SourceToTargetTranslationAttemptResponse(AttemptStatus.Success, "test answer", List.of());

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("test answer", response.getCorrectAnswer());
        assertEquals(QuestionType.SourceToTargetTranslation, response.getQuestionType());
        assertEquals(Collections.emptyList(), response.getSourceWordExplanations());
    }

    @Test
    void constructorShouldThrowExceptionWhenCorrectAnswerIsBlank() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new SourceToTargetTranslationAttemptResponse(AttemptStatus.Success, "", List.of())
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("correctAnswer"));
    }
}
