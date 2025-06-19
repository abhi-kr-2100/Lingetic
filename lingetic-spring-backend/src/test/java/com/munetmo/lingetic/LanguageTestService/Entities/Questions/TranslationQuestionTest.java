package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.TranslationAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.TranslationAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslationQuestionTest {
    private final String defaultSentenceId = "sentence-id";

    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var translateFromLanguage = Language.English;
        var translateToLanguage = Language.Turkish;
        var toTranslateText = "Hello, world";
        var translatedText = "Merhaba, dünya";

        TranslationQuestion question = new TranslationQuestion(id, translateFromLanguage, translateToLanguage, toTranslateText, translatedText, defaultSentenceId, List.of());

        assertEquals(id, question.getID());
        assertEquals(translateToLanguage, question.getLanguage());
        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(defaultSentenceId, question.getSentenceID());
        assertEquals(Collections.emptyList(), question.getSourceWordExplanations());
    }

    @Test
    void constructorShouldCreateValidObjectWithSourceWordExplanation() {
        var id = "test-id";
        var translateFromLanguage = Language.English;
        var translateToLanguage = Language.Turkish;
        var toTranslateText = "Hello, world";
        var translatedText = "Merhaba, dünya";
        var wordExplanations = List.of(
            new WordExplanation(0, "Hello", List.of("noun"), "A greeting"),
            new WordExplanation(7, "world", List.of("noun"), "The Earth")
        );

        TranslationQuestion question = new TranslationQuestion(id, translateFromLanguage, translateToLanguage, toTranslateText, translatedText, defaultSentenceId, wordExplanations);

        assertEquals(id, question.getID());
        assertEquals(translateToLanguage, question.getLanguage());
        assertEquals(QuestionType.Translation, question.getQuestionType());
        assertEquals(defaultSentenceId, question.getSentenceID());
        assertEquals(wordExplanations, question.getSourceWordExplanations());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestion(id, Language.English, Language.Turkish, "Hello", "Merhaba", defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhentoTranslateTextIsInvalid(String toTranslateText) {
        assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestion("id", Language.English, Language.Turkish, toTranslateText, "Merhaba", defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenTranslatedTextIsInvalid(String translatedText) {
        assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestion("id", Language.English, Language.Turkish, "Hello", translatedText, defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenSentenceIdIsInvalid(String sentenceId) {
        assertThrows(IllegalArgumentException.class, () ->
            new TranslationQuestion("id", Language.English, Language.Turkish, "Hello", "Merhaba", sentenceId, List.of())
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectTranslation() {
        var question = new TranslationQuestion(
            "id", Language.English, Language.Turkish, "Hello", "Merhaba", defaultSentenceId, List.of()
        );
        var request = new TranslationAttemptRequest(question.getID(), question.translatedText);

        var response = (TranslationAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.translatedText, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectTranslation() {
        var question = new TranslationQuestion(
            "id", Language.English, Language.Turkish, "Hello", "Merhaba", defaultSentenceId, List.of()
        );
        var request = new TranslationAttemptRequest(question.getID(), "wrong answer");

        var response = (TranslationAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Failure, response.getAttemptStatus());
        assertEquals(question.translatedText, response.getCorrectAnswer());
    }

    @Test
    void getQuestionTypeSpecificDataShouldReturnCorrectMap() {
        var question = new TranslationQuestion(
            "test-id",
            Language.English,
            Language.Turkish,
            "Hello",
            "Merhaba",
            defaultSentenceId,
            List.of()
        );

        var data = question.getQuestionTypeSpecificData();

        assertEquals(Language.English.name(), data.get("translateFromLanguage"));
        assertEquals(Language.Turkish.name(), data.get("translateToLanguage"));
        assertEquals("Hello", data.get("toTranslateText"));
        assertEquals("Merhaba", data.get("translatedText"));
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldCreateEquivalentQuestion() {
        var originalQuestion = new TranslationQuestion(
            "test-id",
            Language.English,
            Language.Turkish,
            "Hello",
            "Merhaba",
            defaultSentenceId,
            List.of()
        );

        var data = originalQuestion.getQuestionTypeSpecificData();

        var newQuestion = TranslationQuestion.createFromQuestionTypeSpecificData(
            originalQuestion.getID(),
            originalQuestion.getLanguage(),
            originalQuestion.getSentenceID(),
            Collections.emptyList(),
            data
        );

        assertEquals(originalQuestion.getID(), newQuestion.getID());
        assertEquals(originalQuestion.getLanguage(), newQuestion.getLanguage());
        assertEquals(originalQuestion.translateFromLanguage, newQuestion.translateFromLanguage);
        assertEquals(originalQuestion.translateToLanguage, newQuestion.translateToLanguage);
        assertEquals(originalQuestion.toTranslateText, newQuestion.toTranslateText);
        assertEquals(originalQuestion.translatedText, newQuestion.translatedText);
        assertEquals(originalQuestion.getSentenceID(), newQuestion.getSentenceID());
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldHandleMissingOptionalFields() {
        var wordExplanations = List.of(
            new WordExplanation(0, "Hello", List.of("noun"), "A greeting")
        );

        Map<String, Object> data = Map.of(
            "translateFromLanguage", Language.English.name(),
            "translateToLanguage", Language.Turkish.name(),
            "toTranslateText", "Hello",
            "translatedText", "Merhaba"
        );

        var newQuestion = TranslationQuestion.createFromQuestionTypeSpecificData(
            "test-id",
            Language.English,
            defaultSentenceId,
            wordExplanations,
            data
        );

        assertEquals(Language.English, newQuestion.translateFromLanguage);
        assertEquals(Language.Turkish, newQuestion.translateToLanguage);
        assertEquals("Hello", newQuestion.toTranslateText);
        assertEquals("Merhaba", newQuestion.translatedText);
        assertEquals(wordExplanations, newQuestion.getSourceWordExplanations());
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhentranslateFromLanguageIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "translateToLanguage", Language.Turkish.name(),
            "toTranslateText", "Hello",
            "translatedText", "Merhaba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                TranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhentranslateToLanguageIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "translateFromLanguage", Language.English.name(),
            "toTranslateText", "Hello",
            "translatedText", "Merhaba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                TranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhentoTranslateTextIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "translateFromLanguage", Language.English.name(),
            "translateToLanguage", Language.Turkish.name(),
            "translatedText", "Merhaba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                TranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenTranslatedTextIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "translateFromLanguage", Language.English.name(),
            "translateToLanguage", Language.Turkish.name(),
            "toTranslateText", "Hello"
        );

        assertThrows(IllegalArgumentException.class, () ->
                TranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenMultipleFieldsAreMissing() {
        Map<String, Object> incompleteData = Map.of(
            "translateFromLanguage", Language.English.name(),
            "translateToLanguage", Language.Turkish.name()
        );

        assertThrows(IllegalArgumentException.class, () ->
                TranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenAllFieldsAreMissing() {
        Map<String, Object> emptyData = Map.of();

        assertThrows(IllegalArgumentException.class, () ->
                TranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        emptyData
                )
        );
    }
}
