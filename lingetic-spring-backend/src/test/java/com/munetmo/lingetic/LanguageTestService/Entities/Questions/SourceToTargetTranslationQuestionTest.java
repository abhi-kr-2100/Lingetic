package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.SourceToTargetTranslationAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.SourceToTargetTranslationAttemptResponse;
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

class SourceToTargetTranslationQuestionTest {
    private final String defaultSentenceId = "sentence-id";

    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var sourceLanguage = Language.English;
        var targetLanguage = Language.Turkish;
        var sourceText = "Hello, world";
        var targetText = "Merhaba, dünya";

        SourceToTargetTranslationQuestion question = new SourceToTargetTranslationQuestion(id, sourceLanguage, targetLanguage, sourceText, targetText, defaultSentenceId, List.of());

        assertEquals(id, question.getID());
        assertEquals(sourceLanguage, question.getLanguage());
        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(defaultSentenceId, question.getSentenceID());
        assertEquals(Collections.emptyList(), question.getSourceWordExplanations());
    }

    @Test
    void constructorShouldCreateValidObjectWithSourceWordExplanation() {
        var id = "test-id";
        var sourceLanguage = Language.English;
        var targetLanguage = Language.Turkish;
        var sourceText = "Hello, world";
        var targetText = "Merhaba, dünya";
        var wordExplanations = List.of(
            new WordExplanation(0, "Hello", List.of("noun"), "A greeting"),
            new WordExplanation(7, "world", List.of("noun"), "The Earth")
        );

        SourceToTargetTranslationQuestion question = new SourceToTargetTranslationQuestion(id, sourceLanguage, targetLanguage, sourceText, targetText, defaultSentenceId, wordExplanations);

        assertEquals(id, question.getID());
        assertEquals(sourceLanguage, question.getLanguage());
        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(defaultSentenceId, question.getSentenceID());
        assertEquals(wordExplanations, question.getSourceWordExplanations());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslationQuestion(id, Language.English, Language.Turkish, "Hello", "Merhaba", defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenSourceTextIsInvalid(String sourceText) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslationQuestion("id", Language.English, Language.Turkish, sourceText, "Merhaba", defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenTargetTextIsInvalid(String targetText) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslationQuestion("id", Language.English, Language.Turkish, "Hello", targetText, defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenSentenceIdIsInvalid(String sentenceId) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslationQuestion("id", Language.English, Language.Turkish, "Hello", "Merhaba", sentenceId, List.of())
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectTranslation() {
        var question = new SourceToTargetTranslationQuestion(
            "id", Language.English, Language.Turkish, "Hello", "Merhaba", defaultSentenceId, List.of()
        );
        var request = new SourceToTargetTranslationAttemptRequest(question.getID(), question.targetText);

        var response = (SourceToTargetTranslationAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.targetText, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectTranslation() {
        var question = new SourceToTargetTranslationQuestion(
            "id", Language.English, Language.Turkish, "Hello", "Merhaba", defaultSentenceId, List.of()
        );
        var request = new SourceToTargetTranslationAttemptRequest(question.getID(), "wrong answer");

        var response = (SourceToTargetTranslationAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Failure, response.getAttemptStatus());
        assertEquals(question.targetText, response.getCorrectAnswer());
    }

    @Test
    void getQuestionTypeSpecificDataShouldReturnCorrectMap() {
        var question = new SourceToTargetTranslationQuestion(
            "test-id",
            Language.English,
            Language.Turkish,
            "Hello",
            "Merhaba",
            defaultSentenceId,
            List.of()
        );

        var data = question.getQuestionTypeSpecificData();

        assertEquals(Language.English.name(), data.get("sourceLanguage"));
        assertEquals(Language.Turkish.name(), data.get("targetLanguage"));
        assertEquals("Hello", data.get("sourceText"));
        assertEquals("Merhaba", data.get("targetText"));
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldCreateEquivalentQuestion() {
        var originalQuestion = new SourceToTargetTranslationQuestion(
            "test-id",
            Language.English,
            Language.Turkish,
            "Hello",
            "Merhaba",
            defaultSentenceId,
            List.of()
        );

        var data = originalQuestion.getQuestionTypeSpecificData();

        var newQuestion = SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
            originalQuestion.getID(),
            originalQuestion.getLanguage(),
            originalQuestion.getSentenceID(),
            Collections.emptyList(),
            data
        );

        assertEquals(originalQuestion.getID(), newQuestion.getID());
        assertEquals(originalQuestion.getLanguage(), newQuestion.getLanguage());
        assertEquals(originalQuestion.sourceLanguage, newQuestion.sourceLanguage);
        assertEquals(originalQuestion.targetLanguage, newQuestion.targetLanguage);
        assertEquals(originalQuestion.sourceText, newQuestion.sourceText);
        assertEquals(originalQuestion.targetText, newQuestion.targetText);
        assertEquals(originalQuestion.getSentenceID(), newQuestion.getSentenceID());
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldHandleMissingOptionalFields() {
        var wordExplanations = List.of(
            new WordExplanation(0, "Hello", List.of("noun"), "A greeting")
        );

        Map<String, Object> data = Map.of(
            "sourceLanguage", Language.English.name(),
            "targetLanguage", Language.Turkish.name(),
            "sourceText", "Hello",
            "targetText", "Merhaba"
        );

        var newQuestion = SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
            "test-id",
            Language.English,
            defaultSentenceId,
            wordExplanations,
            data
        );

        assertEquals(Language.English, newQuestion.sourceLanguage);
        assertEquals(Language.Turkish, newQuestion.targetLanguage);
        assertEquals("Hello", newQuestion.sourceText);
        assertEquals("Merhaba", newQuestion.targetText);
        assertEquals(wordExplanations, newQuestion.getSourceWordExplanations());
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenSourceLanguageIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "targetLanguage", Language.Turkish.name(),
            "sourceText", "Hello",
            "targetText", "Merhaba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenTargetLanguageIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "sourceLanguage", Language.English.name(),
            "sourceText", "Hello",
            "targetText", "Merhaba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenSourceTextIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "sourceLanguage", Language.English.name(),
            "targetLanguage", Language.Turkish.name(),
            "targetText", "Merhaba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenTargetTextIsMissing() {
        Map<String, Object> incompleteData = Map.of(
            "sourceLanguage", Language.English.name(),
            "targetLanguage", Language.Turkish.name(),
            "sourceText", "Hello"
        );

        assertThrows(IllegalArgumentException.class, () ->
                SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
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
            "sourceLanguage", Language.English.name(),
            "targetLanguage", Language.Turkish.name()
        );

        assertThrows(IllegalArgumentException.class, () ->
                SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
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
                SourceToTargetTranslationQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        defaultSentenceId,
                        Collections.emptyList(),
                        emptyData
                )
        );
    }
}
