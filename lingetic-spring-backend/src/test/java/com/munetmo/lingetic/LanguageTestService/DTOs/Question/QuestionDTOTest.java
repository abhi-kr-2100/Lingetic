package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.lib.HashUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionDTOTest {
    private static final String TEST_QUESTION_LIST_ID = "test-list";

    @Test
    void shouldConvertFillInTheBlanksQuestionToDTO() {
        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(
            "q123",
            Language.English,
            "Fill in: ___",
            "This is a hint",
            "test answer",
            5,
            TEST_QUESTION_LIST_ID
        );

        QuestionDTO dto = QuestionDTO.fromQuestion(question);

        assertInstanceOf(FillInTheBlanksQuestionDTO.class, dto);
        assertEquals("q123", dto.getID());
        assertEquals(QuestionType.FillInTheBlanks, dto.getQuestionType());
        assertEquals("Fill in: ___", ((FillInTheBlanksQuestionDTO) dto).getText());
        assertEquals("This is a hint", ((FillInTheBlanksQuestionDTO) dto).getHint());

        var expectedFullText = "Fill in: test answer";
        var expectedDigest = HashUtils.sha1(String.format("%s_%s", expectedFullText.trim(), question.getLanguage().toString()));
        assertEquals(expectedDigest, ((FillInTheBlanksQuestionDTO) dto).getFullTextDigest());
    }
}
