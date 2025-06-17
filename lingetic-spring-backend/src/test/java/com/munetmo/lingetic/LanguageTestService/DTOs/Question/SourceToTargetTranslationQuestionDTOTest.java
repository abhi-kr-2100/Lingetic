package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SourceToTargetTranslationQuestionDTOTest {
    private static final String TEST_SENTENCE_ID = "test-sentence-id";
    private static final String TEST_SOURCE_TEXT = "Hello, world!";
    private static final Language TEST_SOURCE_LANGUAGE = Language.English;
    private static final Language TEST_TARGET_LANGUAGE = Language.French;

    @Test
    void shouldCreateQuestionDTO() {
        SourceToTargetTranslationQuestionDTO question = new SourceToTargetTranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                TEST_SOURCE_TEXT,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getSourceLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTargetLanguage());
        assertEquals(TEST_SOURCE_TEXT, question.getSourceText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldHaveCorrectStaticQuestionType() {
        assertEquals(QuestionType.SourceToTargetTranslation, SourceToTargetTranslationQuestionDTO.questionType);
    }

    @Test
    void shouldThrowExceptionWhenSourceTextIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> 
            new SourceToTargetTranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                "",
                TEST_SENTENCE_ID));
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("sourceText"));
    }

    @Test
    void shouldThrowExceptionWhenSourceTextIsWhitespace() {
        var exception = assertThrows(IllegalArgumentException.class, () -> 
            new SourceToTargetTranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                "   ",
                TEST_SENTENCE_ID));
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("sourceText"));
    }

    @Test
    void shouldThrowExceptionWhenSentenceIdIsBlank() {
        var exception = assertThrows(IllegalArgumentException.class, () -> 
            new SourceToTargetTranslationQuestionDTO(
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
            new SourceToTargetTranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                TEST_SOURCE_TEXT,
                "   "));
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("sentenceID"));
    }

    @Test
    void shouldCreateQuestionDTOWithDifferentLanguages() {
        SourceToTargetTranslationQuestionDTO question = new SourceToTargetTranslationQuestionDTO(
                Language.Turkish,
                Language.Swedish,
                "Merhaba dünya!",
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(Language.Turkish, question.getSourceLanguage());
        assertEquals(Language.Swedish, question.getTargetLanguage());
        assertEquals("Merhaba dünya!", question.getSourceText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithSameSourceAndTargetLanguage() {
        SourceToTargetTranslationQuestionDTO question = new SourceToTargetTranslationQuestionDTO(
                Language.English,
                Language.English,
                TEST_SOURCE_TEXT,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(Language.English, question.getSourceLanguage());
        assertEquals(Language.English, question.getTargetLanguage());
        assertEquals(TEST_SOURCE_TEXT, question.getSourceText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithJapaneseLanguage() {
        SourceToTargetTranslationQuestionDTO question = new SourceToTargetTranslationQuestionDTO(
                Language.JapaneseModifiedHepburn,
                Language.English,
                "Konnichiwa sekai!",
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(Language.JapaneseModifiedHepburn, question.getSourceLanguage());
        assertEquals(Language.English, question.getTargetLanguage());
        assertEquals("Konnichiwa sekai!", question.getSourceText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithLongSourceText() {
        String longText = "This is a very long source text that contains multiple sentences. " +
                         "It should still be valid as long as it's not blank. " +
                         "The validation only checks for blank strings, not length limits.";
        
        SourceToTargetTranslationQuestionDTO question = new SourceToTargetTranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                longText,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getSourceLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTargetLanguage());
        assertEquals(longText, question.getSourceText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithSpecialCharactersInSourceText() {
        String textWithSpecialChars = "¡Hola, mundo! ¿Cómo estás? 123 @#$%^&*()";
        
        SourceToTargetTranslationQuestionDTO question = new SourceToTargetTranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                textWithSpecialChars,
                TEST_SENTENCE_ID);

        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getSourceLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTargetLanguage());
        assertEquals(textWithSpecialChars, question.getSourceText());
        assertEquals(TEST_SENTENCE_ID, question.getSentenceID());
    }

    @Test
    void shouldCreateQuestionDTOWithUUIDSentenceId() {
        String uuidSentenceId = "550e8400-e29b-41d4-a716-446655440000";
        
        SourceToTargetTranslationQuestionDTO question = new SourceToTargetTranslationQuestionDTO(
                TEST_SOURCE_LANGUAGE,
                TEST_TARGET_LANGUAGE,
                TEST_SOURCE_TEXT,
                uuidSentenceId);

        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(TEST_SOURCE_LANGUAGE, question.getSourceLanguage());
        assertEquals(TEST_TARGET_LANGUAGE, question.getTargetLanguage());
        assertEquals(TEST_SOURCE_TEXT, question.getSourceText());
        assertEquals(uuidSentenceId, question.getSentenceID());
    }
}
