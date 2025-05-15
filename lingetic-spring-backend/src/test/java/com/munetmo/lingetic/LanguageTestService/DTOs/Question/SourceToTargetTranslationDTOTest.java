package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SourceToTargetTranslationDTOTest {
    @Test
    void shouldCreateQuestionDTO() {
        SourceToTargetTranslationDTO question = new SourceToTargetTranslationDTO(
            "q123",
            Language.Swedish,
            "I'm David."
        );

        assertEquals("q123", question.getID());
        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals("I'm David.", question.getTranslation());
        assertEquals(Language.Swedish, question.getLanguage());
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new SourceToTargetTranslationDTO(
            "", Language.Swedish, "I'm David."));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    void shouldThrowExceptionWhenTranslationIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new SourceToTargetTranslationDTO(
            "q123", Language.Swedish, ""));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("translation"));
    }
}
