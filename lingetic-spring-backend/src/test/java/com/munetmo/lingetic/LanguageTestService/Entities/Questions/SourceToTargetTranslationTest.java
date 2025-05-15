package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.SourceToTargetTranslationAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.SourceToTargetTranslationAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SourceToTargetTranslationTest {
    private final String defaultQuestionListId = "list-id";

    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var language = Language.Swedish;
        var targetText = "Jag heter David.";
        var translation = "I'm David.";

        SourceToTargetTranslation question = new SourceToTargetTranslation(id, language, targetText, translation, 5, defaultQuestionListId);

        assertEquals(id, question.getID());
        assertEquals(language, question.getLanguage());
        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(targetText, question.targetText);
        assertEquals(translation, question.translation);
        assertEquals(5, question.difficulty);
        assertEquals(defaultQuestionListId, question.getQuestionListID());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslation(id, Language.Swedish, "Jag heter David.", "I'm David.", 5, defaultQuestionListId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenQuestionListIdIsInvalid(String questionListId) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslation("id", Language.Swedish, "Jag heter David.", "I'm David.", 5, questionListId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenTargetTextIsInvalid(String targetText) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslation("id", Language.Swedish, targetText, "I'm David.", 5, defaultQuestionListId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenTranslationIsInvalid(String translation) {
        assertThrows(IllegalArgumentException.class, () ->
            new SourceToTargetTranslation("id", Language.Swedish, "Jag heter David.", translation, 5, defaultQuestionListId)
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectAnswer() {
        SourceToTargetTranslation question = new SourceToTargetTranslation(
            "id", Language.Swedish, "Jag heter David.", "I'm David.", 5, defaultQuestionListId
        );
        var request = new SourceToTargetTranslationAttemptRequest(question.getID(), question.targetText);

        var response = (SourceToTargetTranslationAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.targetText, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectAnswer() {
        SourceToTargetTranslation question = new SourceToTargetTranslation(
            "id", Language.Swedish, "Jag heter David.", "I'm David.", 5, defaultQuestionListId
        );
        var request = new SourceToTargetTranslationAttemptRequest(question.getID(), "wrong answer");

        var response = (SourceToTargetTranslationAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Failure, response.getAttemptStatus());
        assertEquals(question.targetText, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldThrowIfAttemptTypeDoesNotMatch() {
        var question = new SourceToTargetTranslation(
            "id", Language.Swedish, "Jag heter David.", "I'm David.", 5, defaultQuestionListId
        );

        var request = new FillInTheBlanksAttemptRequest(question.getID(), "wrong answer");

        assertThrows(IllegalArgumentException.class, () -> question.assessAttempt(request));
    }

    @Test
    void getQuestionTypeSpecificDataShouldReturnCorrectMap() {
        var question = new SourceToTargetTranslation(
            "test-id",
            Language.Swedish,
            "Jag heter David.",
            "I'm David.",
            5,
            defaultQuestionListId
        );

        var data = question.getQuestionTypeSpecificData();

        assertEquals("Jag heter David.", data.get("targetText"));
        assertEquals("I'm David.", data.get("translation"));
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldCreateEquivalentQuestion() {
        var originalQuestion = new SourceToTargetTranslation(
            "test-id",
            Language.Swedish,
            "Jag heter David.",
            "I'm David.",
            5,
            defaultQuestionListId
        );

        var data = originalQuestion.getQuestionTypeSpecificData();
        var newQuestion = SourceToTargetTranslation.createFromQuestionTypeSpecificData(
            originalQuestion.getID(),
            originalQuestion.getLanguage(),
            originalQuestion.getDifficulty(),
            originalQuestion.getQuestionListID(),
            data
        );

        assertEquals(originalQuestion.getID(), newQuestion.getID());
        assertEquals(originalQuestion.getLanguage(), newQuestion.getLanguage());
        assertEquals(originalQuestion.getDifficulty(), newQuestion.getDifficulty());
        assertEquals(originalQuestion.getQuestionListID(), newQuestion.getQuestionListID());
        assertEquals(originalQuestion.targetText, newQuestion.targetText);
        assertEquals(originalQuestion.translation, newQuestion.translation);
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenTargetTextIsMissing() {
        Map<String, Object> data = Map.of(
            "translation", "I'm David."
        );

        assertThrows(IllegalArgumentException.class, () ->
            SourceToTargetTranslation.createFromQuestionTypeSpecificData("id", Language.Swedish, 5, defaultQuestionListId, data)
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenTranslationIsMissing() {
        Map<String, Object> data = Map.of(
            "targetText", "Jag heter David."
        );

        assertThrows(IllegalArgumentException.class, () ->
            SourceToTargetTranslation.createFromQuestionTypeSpecificData("id", Language.Swedish, 5, defaultQuestionListId, data)
        );
    }
}
