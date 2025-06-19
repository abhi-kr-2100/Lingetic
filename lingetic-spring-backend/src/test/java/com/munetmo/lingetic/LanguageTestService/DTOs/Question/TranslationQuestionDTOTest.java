package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TranslationQuestionDTOTest {
    private static final String TEST_SENTENCE_ID = "test-sentence-id";
    private static final String TEST_SOURCE_TEXT = "Hello, world!";
    private static final Language TEST_SOURCE_LANGUAGE = Language.English;
    private static final Language TEST_TARGET_LANGUAGE = Language.French;

    @Test
    void shouldCreateQuestionDTO() {
        TranslationQuestionDTO question = new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                TEST_SOURCE_TEXT,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getTranslateFromLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTranslateToLanguage());
        assertEquals(TEST_SOURCE_TEXT, question.getToTranslateText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldHaveCorrectStaticQuestionType() {
        assertEquals(QuestionType.Translation, TranslationQuestionDTO.questionType);
    }

    @Test
    void shouldThrowExceptionWhenToTranslateTextIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                "",
                TEST_SENTENCE_ID));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("toTranslateText"));
    }

    @Test
    void shouldThrowExceptionWhenToTranslateTextIsWhitespace() {
        var exception = assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                "   ",
                TEST_SENTENCE_ID));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("toTranslateText"));
    }

    @Test
    void shouldThrowExceptionWhenSentenceIdIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                TEST_SOURCE_TEXT,
                ""));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("sentenceID"));
    }

    @Test
    void shouldThrowExceptionWhenSentenceIdIsWhitespace() {
        var exception = assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                TEST_SOURCE_TEXT,
                "   "));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("sentenceID"));
    }

    @Test
    void shouldCreateQuestionDTOWithDifferentLanguages() {
        TranslationQuestionDTO question = new TranslationQuestionDTO(
                Language.Turkish,
                Language.Swedish,
                "Merhaba dünya!",
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(Language.Turkish, question.getTranslateFromLanguage());
        assertEquals(Language.Swedish, question.getTranslateToLanguage());
        assertEquals("Merhaba dünya!", question.getToTranslateText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithSameSourceAndtranslateToLanguage() {
        TranslationQuestionDTO question = new TranslationQuestionDTO(
                Language.English,
                Language.English,
                TEST_SOURCE_TEXT,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(Language.English, question.getTranslateFromLanguage());
        assertEquals(Language.English, question.getTranslateToLanguage());
        assertEquals(TEST_SOURCE_TEXT, question.getToTranslateText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithJapaneseLanguage() {
        TranslationQuestionDTO question = new TranslationQuestionDTO(
                Language.JapaneseModifiedHepburn,
                Language.English,
                "Konnichiwa sekai!",
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(Language.JapaneseModifiedHepburn, question.getTranslateFromLanguage());
        assertEquals(Language.English, question.getTranslateToLanguage());
        assertEquals("Konnichiwa sekai!", question.getToTranslateText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithLongToTranslateText() {
        String longText = "This is a very long toTranslate text that contains multiple sentences. " +
                         "It should still be valid as long as it's not blank. " +
                         "The validation only checks for blank strings, not length limits.";

        TranslationQuestionDTO question = new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                longText,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getTranslateFromLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTranslateToLanguage());
        assertEquals(longText, question.getToTranslateText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithSpecialCharactersInToTranslateText() {
        String textWithSpecialChars = "¡Hola, mundo! ¿Cómo estás? 123 @#$%^&*()";

        TranslationQuestionDTO question = new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                textWithSpecialChars,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getTranslateFromLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTranslateToLanguage());
        assertEquals(textWithSpecialChars, question.getToTranslateText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithUUIDSentenceId() {
        String uuidSentenceId = "550e8400-e29b-41d4-a716-446655440000";

        TranslationQuestionDTO question = new TranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                TEST_SOURCE_TEXT,
                uuidSentenceId);

        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getTranslateFromLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTranslateToLanguage());
        assertEquals(TEST_SOURCE_TEXT, question.getToTranslateText());
        assertEquals(uuidSentenceId, question.getSentenceID());
    }
}
