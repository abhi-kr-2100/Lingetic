package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

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
        assertEquals("FillInTheBlanks", question.getType());
        assertEquals("Fill in: ___", question.text);
        assertEquals("This is a hint", question.hint);
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new FillInTheBlanksQuestionDTO(null, "Fill in: ___", "This is a hint");
        });
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FillInTheBlanksQuestionDTO("", "Fill in: ___", "This is a hint");
        });
    }

    @Test
    void shouldThrowExceptionWhenTextIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new FillInTheBlanksQuestionDTO("q123", null, "This is a hint");
        });
    }

    @Test
    void shouldThrowExceptionWhenTextIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FillInTheBlanksQuestionDTO("q123", "", "This is a hint");
        });
    }

    @Test
    void shouldCreateQuestionDTOWithoutHint() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                "Fill in: ___",
                null);

        assertEquals("q123", question.getID());
        assertEquals("FillInTheBlanks", question.getType());
        assertEquals("Fill in: ___", question.text);
        assertEquals("", question.hint);
    }

    @Test
    void shouldCreateQuestionDTOWithEmptyHint() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "q123",
                "Fill in: ___",
                "");

        assertEquals("q123", question.getID());
        assertEquals("FillInTheBlanks", question.getType());
        assertEquals("Fill in: ___", question.text);
        assertEquals("", question.hint);
    }
}
