package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.SourceToTargetTranslationQuestion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionDTOTest {
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
        assertEquals(QuestionType.FillInTheBlanks, dto.getQuestionType());
        assertEquals(TEST_SENTENCE_ID, dto.getSentenceID());
        assertEquals("Fill in: ___", ((FillInTheBlanksQuestionDTO) dto).getText());
        assertEquals("This is a hint", ((FillInTheBlanksQuestionDTO) dto).getHint());
    }

    @Test
    void shouldConvertSourceToTargetTranslationQuestionToDTO() {
        var question = new SourceToTargetTranslationQuestion(
            "q123",
            Language.English,
            Language.Turkish,
            "Hello",
            "Merhaba",
            TEST_SENTENCE_ID,
            List.of()
        );

        QuestionDTO dto = QuestionDTO.fromQuestion(question);

        assertInstanceOf(SourceToTargetTranslationQuestionDTO.class, dto);
        assertEquals(QuestionType.SourceToTargetTranslation, dto.getQuestionType());
        assertEquals(TEST_SENTENCE_ID, dto.getSentenceID());
        assertEquals("Hello", ((SourceToTargetTranslationQuestionDTO) dto).getSourceText());
        assertEquals(Language.English, ((SourceToTargetTranslationQuestionDTO) dto).getSourceLanguage());
        assertEquals(Language.Turkish, ((SourceToTargetTranslationQuestionDTO) dto).getTargetLanguage());
    }
}
