package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class FillInTheBlanksQuestionTest {
    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var questionText = "Fill in the ___";
        var hint = "test hint";
        var answer = "blank";

        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(id, questionText, hint, answer);

        assertEquals(id, question.getID());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(questionText, question.questionText);
        assertEquals(hint, question.hint);
        assertEquals(answer, question.answer);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion(id, "Fill in the ___", "hint", "answer")
        );
    }

    @Test
    void constructorShouldThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion(null, "Fill in the ___", "hint", "answer")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", "No blank here", "Multiple___ ___blanks", "Wrong blank --"})
    void constructorShouldThrowExceptionWhenQuestionTextIsInvalid(String questionText) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", questionText, "hint", "answer")
        );
    }

    @Test
    void constructorShouldThrowExceptionWhenQuestionTextIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", null, "hint", "answer")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenAnswerIsInvalid(String answer) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", "Fill in the ___", "hint", answer)
        );
    }

    @Test
    void constructorShouldThrowExceptionWhenAnswerIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", "Fill in the ___", "hint", null)
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectAnswer() {
        var question = new FillInTheBlanksQuestion(
            "id", "Fill in the ___", "hint", "correct"
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), question.answer);

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectAnswer() {
        var question = new FillInTheBlanksQuestion(
            "id", "Fill in the ___", "hint", "correct"
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "wrong");

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Failure, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }
}
