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
    void constructorShouldThrowExceptionWhenCorrectAnswerIsBlank() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FillInTheBlanksAttemptResponse(AttemptStatus.Success, "")
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("correctAnswer"));
    }
}
