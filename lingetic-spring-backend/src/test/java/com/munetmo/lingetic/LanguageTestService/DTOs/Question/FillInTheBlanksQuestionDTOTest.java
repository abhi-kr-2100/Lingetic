package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FillInTheBlanksQuestionDTOTest {

    @Test
    void shouldCreateQuestionDTO() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                Language.English,
                "Fill in: ___",
                "digest123",
                "This is a hint");

        assertEquals("q123", question.getID());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("digest123", question.getFullTextDigest());
        assertEquals("This is a hint", question.getHint());
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "", Language.English, "Fill in: ___", "digest123", "This is a hint"));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    void shouldThrowExceptionWhenGetTextIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "q123", Language.English, "", "digest123", "This is a hint"));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("text"));
    }

    @Test
    void shouldThrowExceptionWhenGetFullTextDigestIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "q123", Language.English, "Fill in: ___", "\t", "This is a hint"));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("fullTextDigest"));
    }

    @Test
    void shouldCreateQuestionDTOWithoutGetHint() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                Language.English,
                "Fill in: ___",
                "digest123",
                null);

        assertEquals("q123", question.getID());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("digest123", question.getFullTextDigest());
        assertEquals("", question.getHint());
    }

    @Test
    void shouldCreateQuestionDTOWithEmptyGetHint() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                Language.English,
                "Fill in: ___",
                "digest123",
                "");

        assertEquals("q123", question.getID());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("digest123", question.getFullTextDigest());
        assertEquals("", question.getHint());
    }
}
