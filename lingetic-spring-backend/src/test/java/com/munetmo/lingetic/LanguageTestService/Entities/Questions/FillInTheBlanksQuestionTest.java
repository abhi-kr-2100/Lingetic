package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class FillInTheBlanksQuestionTest {
    private final QuestionList defaultQuestionList = new QuestionList("list-id", "Test QuestionList");
    private final Supplier<QuestionList> defaultQuestionListSupplier = () -> defaultQuestionList;

    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var language = Language.English;
        var questionText = "Fill in the ___";
        var hint = "test hint";
        var answer = "blank";

        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(id, language, questionText, hint, answer, 5, defaultQuestionListSupplier);

        assertEquals(id, question.getID());
        assertEquals(language, question.getLanguage());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(questionText, question.questionText);
        assertEquals(hint, question.hint);
        assertEquals(answer, question.answer);
        assertEquals(5, question.difficulty);
        assertEquals(defaultQuestionList, question.getQuestionList());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion(id, Language.English, "Fill in the ___", "hint", "answer", 5, defaultQuestionListSupplier)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", "No blank here", "Multiple___ ___blanks", "Wrong blank --"})
    void constructorShouldThrowExceptionWhenQuestionTextIsInvalid(String questionText) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, questionText, "hint", "answer", 5, defaultQuestionListSupplier)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenAnswerIsInvalid(String answer) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, "Fill in the ___", "hint", answer, 5, defaultQuestionListSupplier)
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectAnswer() {
        var question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "correct", 5, defaultQuestionListSupplier
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), question.answer);

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectAnswer() {
        var question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "correct", 5, defaultQuestionListSupplier
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "wrong");

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Failure, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }
}
