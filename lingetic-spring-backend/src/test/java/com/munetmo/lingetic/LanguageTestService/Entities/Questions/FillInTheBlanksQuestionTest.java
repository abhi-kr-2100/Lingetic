package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FillInTheBlanksQuestionTest {
    private final String defaultQuestionListId ="list-id";

    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var language = Language.English;
        var questionText = "Fill in the ___";
        var hint = "test hint";
        var answer = "blank";

        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(id, language, questionText, hint, answer, 5, defaultQuestionListId);

        assertEquals(id, question.getID());
        assertEquals(language, question.getLanguage());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(questionText, question.questionText);
        assertEquals(hint, question.hint);
        assertEquals(answer, question.answer);
        assertEquals(5, question.difficulty);
        assertEquals(defaultQuestionListId, question.getQuestionListID());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion(id, Language.English, "Fill in the ___", "hint", "answer", 5, defaultQuestionListId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenQuestionListIdIsInvalid(String questionListId) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, "Fill in the ___", "hint", "answer", 5, questionListId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", "No blank here", "Multiple___ ___blanks", "Wrong blank --"})
    void constructorShouldThrowExceptionWhenQuestionTextIsInvalid(String questionText) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, questionText, "hint", "answer", 5, defaultQuestionListId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenAnswerIsInvalid(String answer) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, "Fill in the ___", "hint", answer, 5, defaultQuestionListId)
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectAnswer() {
        var question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "correct", 5, defaultQuestionListId
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), question.answer);

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectAnswer() {
        var question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "correct", 5, defaultQuestionListId
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "wrong");

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Failure, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }

    @Test
    void getQuestionTypeSpecificDataShouldReturnCorrectMap() {
        var question = new FillInTheBlanksQuestion(
            "test-id",
            Language.English,
            "Fill in the ___",
            "test hint",
            "blank",
            5,
            defaultQuestionListId
        );

        var data = question.getQuestionTypeSpecificData();

        assertEquals("Fill in the ___", data.get("questionText"));
        assertEquals("test hint", data.get("hint"));
        assertEquals("blank", data.get("answer"));
        assertEquals(3, data.size());
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldCreateEquivalentQuestion() {
        var originalQuestion = new FillInTheBlanksQuestion(
            "test-id",
            Language.English,
            "Fill in the ___",
            "test hint",
            "blank",
            5,
            defaultQuestionListId
        );

        var data = originalQuestion.getQuestionTypeSpecificData();
        var newQuestion = FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
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
        assertEquals(originalQuestion.questionText, ((FillInTheBlanksQuestion)newQuestion).questionText);
        assertEquals(originalQuestion.hint, ((FillInTheBlanksQuestion)newQuestion).hint);
        assertEquals(originalQuestion.answer, ((FillInTheBlanksQuestion)newQuestion).answer);
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldHandleMissingOptionalFields() {
        Map<String, Object> data = Map.of(
            "questionText", "Fill in the ___",
            "answer", "blank"
        );

        var newQuestion = FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
            "test-id",
            Language.English,
            5,
            defaultQuestionListId,
            data
        );

        assertEquals("", newQuestion.hint);
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenQuestionTextIsMissing() {
        Map<String, Object> incompleteData = Map.of("answer", "blank");

        assertThrows(IllegalArgumentException.class, () ->
                FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        5,
                        defaultQuestionListId,
                        incompleteData
                )
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenAnswerIsMissing() {
        Map<String, Object> incompleteData = Map.of("questionText", "Fill in the ___");

        assertThrows(IllegalArgumentException.class, () ->
                FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
                        "test-id",
                        Language.English,
                        5,
                        defaultQuestionListId,
                        incompleteData
                )
        );
    }
}
