package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SourceToTargetTranslationAttemptResponseTest {
    @Test
    void shouldCreateResponse() {
        var response = new SourceToTargetTranslationAttemptResponse(AttemptStatus.Success, "Jag heter David.");

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(QuestionType.SourceToTargetTranslation, response.getQuestionType());
        assertEquals("Jag heter David.", response.getCorrectAnswer());
    }

    @Test
    void shouldThrowExceptionForBlankCorrectAnswer() {
        assertThrows(IllegalArgumentException.class,
            () -> new SourceToTargetTranslationAttemptResponse(AttemptStatus.Success, ""));
    }
}
