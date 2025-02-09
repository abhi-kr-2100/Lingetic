package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FillInTheBlanksQuestionDTOTest {

    @Test
    void shouldCreateQuestionDTO() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                "Fill in: ___",
                "This is a hint");

        assertEquals("q123", question.getID());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("This is a hint", question.getHint());
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                null, "Fill in: ___", "This is a hint"));
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "", "Fill in: ___", "This is a hint"));
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    void shouldThrowExceptionWhenGetTextIsNull() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "q123", null, "This is a hint"));
        assertTrue(exception.getMessage().contains("text"));
    }

    @Test
    void shouldThrowExceptionWhenGetTextIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "q123", "", "This is a hint"));
        assertTrue(exception.getMessage().contains("text"));
    }

    @Test
    void shouldCreateQuestionDTOWithoutGetHint() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                "Fill in: ___",
                null);

        assertEquals("q123", question.getID());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("", question.getHint());
    }

    @Test
    void shouldCreateQuestionDTOWithEmptyGetHint() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                "Fill in: ___",
                "");

        assertEquals("q123", question.getID());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("", question.getHint());
    }
}
