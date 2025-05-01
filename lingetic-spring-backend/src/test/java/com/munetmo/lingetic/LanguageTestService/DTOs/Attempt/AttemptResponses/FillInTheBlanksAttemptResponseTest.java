package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion.WordExplanation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FillInTheBlanksAttemptResponseTest {
    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var response = new FillInTheBlanksAttemptResponse(AttemptStatus.Success, "test answer");

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("test answer", response.getCorrectAnswer());
        assertEquals(QuestionType.FillInTheBlanks, response.getQuestionType());
        assertEquals(List.of(), response.getExplanation());
    }

    @Test
    void constructorShouldThrowExceptionWhenCorrectAnswerIsBlank() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FillInTheBlanksAttemptResponse(AttemptStatus.Success, "")
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("correctAnswer"));
    }

    @Test
    void constructorShouldCreateValidObjectWithExplanation() {
        var explanation = java.util.List.of(
            new WordExplanation(1, "word1", List.of("p1"), "c1"),
            new WordExplanation(2, "word2", List.of("p2"), "c2")
        );
        var response = new FillInTheBlanksAttemptResponse(AttemptStatus.Success, "test answer", explanation);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("test answer", response.getCorrectAnswer());
        assertEquals(QuestionType.FillInTheBlanks, response.getQuestionType());
        assertEquals(explanation, response.getExplanation());
    }

    @Test
    void constructorShouldCreateValidObjectWithNullExplanation() {
        var response = new FillInTheBlanksAttemptResponse(AttemptStatus.Success, "test answer", null);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("test answer", response.getCorrectAnswer());
        assertEquals(QuestionType.FillInTheBlanks, response.getQuestionType());
        assertNotNull(response.getExplanation());
        assertTrue(response.getExplanation().isEmpty());
    }
}
