package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionDTOTest {
    private static final String TEST_QUESTION_LIST_ID = "test-list";
    private static final String TEST_SENTENCE_ID = "test-sentence-id";

    @Test
    void shouldConvertFillInTheBlanksQuestionToDTO() {
        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(
            "q123",
            Language.English,
            "Fill in: ___",
            "This is a hint",
            "test answer",
            TEST_SENTENCE_ID,
            List.of()
        );

        QuestionDTO dto = QuestionDTO.fromQuestion(question);

        assertInstanceOf(FillInTheBlanksQuestionDTO.class, dto);
        assertEquals("q123", dto.getID());
        assertEquals(QuestionType.FillInTheBlanks, dto.getQuestionType());
        assertEquals("Fill in: ___", ((FillInTheBlanksQuestionDTO) dto).getText());
        assertEquals("This is a hint", ((FillInTheBlanksQuestionDTO) dto).getHint());
    }
}
