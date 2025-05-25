package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FillInTheBlanksQuestionDTOTest {
    private static final String TEST_SENTENCE_ID = "test-sentence-id";

    @Test
    void shouldCreateQuestionDTO() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "Fill in: ___",
                "This is a hint",
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("This is a hint", question.getHint());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldThrowExceptionWhenSentenceIdIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "Fill in: ___", "This is a hint", ""));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("sentenceID"));
    }

    @Test
    void shouldThrowExceptionWhenGetTextIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> new FillInTheBlanksQuestionDTO(
                "", "This is a hint", TEST_SENTENCE_ID));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("text"));
    }

    @Test
    void shouldCreateQuestionDTOWithoutGetHint() {
        FillInTheBlanksQuestionDTO question = new FillInTheBlanksQuestionDTO(
                "Fill in: ___",
                null,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals("Fill in: ___", question.getText());
        assertEquals("", question.getHint());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }
}
