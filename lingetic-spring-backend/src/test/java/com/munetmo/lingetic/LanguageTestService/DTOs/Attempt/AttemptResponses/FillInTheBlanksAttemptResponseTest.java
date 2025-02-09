package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FillInTheBlanksAttemptResponseTest {
    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var response = new FillInTheBlanksAttemptResponse(AttemptStatus.Success, "test answer");

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("test answer", response.getCorrectAnswer());
        assertEquals(QuestionType.FillInTheBlanks, response.getQuestionType());
    }

    @Test
    void constructorShouldThrowExceptionWhenAttemptStatusIsNull() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FillInTheBlanksAttemptResponse(null, "test answer")
        );
        assertTrue(exception.getMessage().contains("attemptStatus"));
    }

    @Test
    void constructorShouldThrowExceptionWhenCorrectAnswerIsNull() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FillInTheBlanksAttemptResponse(AttemptStatus.Success, null)
        );
        assertTrue(exception.getMessage().contains("correctAnswer"));
    }

    @Test
    void constructorShouldThrowExceptionWhenCorrectAnswerIsBlank() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FillInTheBlanksAttemptResponse(AttemptStatus.Success, "")
        );
        assertTrue(exception.getMessage().contains("correctAnswer"));
    }
}
