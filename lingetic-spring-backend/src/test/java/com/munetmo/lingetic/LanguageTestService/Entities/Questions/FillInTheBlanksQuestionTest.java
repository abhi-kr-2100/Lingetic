package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FillInTheBlanksQuestionTest {
    private final String defaultSentenceId = "sentence-id";

    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var language = Language.English;
        var questionText = "Fill in the ___";
        var hint = "test hint";
        var answer = "blank";

        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(id, language, questionText, hint, answer, defaultSentenceId);

        assertEquals(id, question.getID());
        assertEquals(language, question.getLanguage());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(questionText, question.questionText);
        assertEquals(hint, question.hint);
        assertEquals(answer, question.answer);
        assertEquals(defaultSentenceId, question.getSentenceID());
    }



    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion(id, Language.English, "Fill in the ___", "hint", "answer", defaultSentenceId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", "No blank here", "Multiple___ ___blanks", "Wrong blank --"})
    void constructorShouldThrowExceptionWhenQuestionTextIsInvalid(String questionText) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, questionText, "hint", "answer", defaultSentenceId)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenAnswerIsInvalid(String answer) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, "Fill in the ___", "hint", answer, defaultSentenceId)
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectAnswer() {
        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "blank", defaultSentenceId
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), question.answer);

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectAnswer() {
        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "blank", defaultSentenceId
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
            defaultSentenceId
        );

        var data = question.getQuestionTypeSpecificData();

        assertEquals("Fill in the ___", data.get("questionText"));
        assertEquals("test hint", data.get("hint"));
        assertEquals("blank", data.get("answer"));
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldCreateEquivalentQuestion() {
        var originalQuestion = new FillInTheBlanksQuestion(
            "test-id",
            Language.English,
            "Fill in the ___",
            "test hint",
            "blank",
            defaultSentenceId
        );

        var data = originalQuestion.getQuestionTypeSpecificData();
        
        var newQuestion = FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
            originalQuestion.getID(),
            originalQuestion.getLanguage(),
            originalQuestion.getSentenceID(),
            data
        );
        
        assertEquals(originalQuestion.getID(), newQuestion.getID());
        assertEquals(originalQuestion.getLanguage(), newQuestion.getLanguage());
        assertEquals(originalQuestion.questionText, ((FillInTheBlanksQuestion)newQuestion).questionText);
        assertEquals(originalQuestion.hint, ((FillInTheBlanksQuestion)newQuestion).hint);
        assertEquals(originalQuestion.answer, ((FillInTheBlanksQuestion)newQuestion).answer);
        assertEquals(originalQuestion.getSentenceID(), newQuestion.getSentenceID());
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
            defaultSentenceId,
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
                        defaultSentenceId,
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
                        defaultSentenceId,
                        incompleteData
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenSentenceIdIsInvalid(String sentenceId) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, "Fill in the ___", "hint", "answer", sentenceId)
        );
    }


}
