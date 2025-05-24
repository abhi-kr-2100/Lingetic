package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
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

class FillInTheBlanksQuestionTest {
    private final String defaultSentenceId = "sentence-id";

    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var language = Language.English;
        var questionText = "Fill in the ___";
        var hint = "test hint";
        var answer = "blank";

        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(id, language, questionText, hint, answer, defaultSentenceId, List.of());

        assertEquals(id, question.getID());
        assertEquals(language, question.getLanguage());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(questionText, question.questionText);
        assertEquals(hint, question.hint);
        assertEquals(answer, question.answer);
        assertEquals(defaultSentenceId, question.getSentenceID());
        assertEquals(Collections.emptyList(), question.getSourceWordExplanations());
    }

    @Test
    void constructorShouldCreateValidObjectWithSourceWordExplanation() {
        var id = "test-id";
        var language = Language.English;
        var questionText = "Fill in the ___";
        var hint = "test hint";
        var answer = "blank";
        var wordExplanations = List.of(
            new WordExplanation(0, "Fill", List.of("verb"), "To complete something"),
            new WordExplanation(8, "the", List.of("article"), "Definite article")
        );

        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(id, language, questionText, hint, answer, defaultSentenceId, wordExplanations);

        assertEquals(id, question.getID());
        assertEquals(language, question.getLanguage());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(questionText, question.questionText);
        assertEquals(hint, question.hint);
        assertEquals(answer, question.answer);
        assertEquals(defaultSentenceId, question.getSentenceID());
        assertEquals(wordExplanations, question.getSourceWordExplanations());
    }



    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion(id, Language.English, "Fill in the ___", "hint", "answer", defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", "No blank here", "Multiple___ ___blanks", "Wrong blank --"})
    void constructorShouldThrowExceptionWhenQuestionTextIsInvalid(String questionText) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, questionText, "hint", "answer", defaultSentenceId, List.of())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenAnswerIsInvalid(String answer) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, "Fill in the ___", "hint", answer, defaultSentenceId, List.of())
        );
    }

    @Test
    void assessAttemptShouldReturnSuccessForCorrectAnswer() {
        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "blank", defaultSentenceId, List.of()
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), question.answer);

        var response = (FillInTheBlanksAttemptResponse) question.assessAttempt(request);

        assertEquals(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals(question.answer, response.getCorrectAnswer());
    }

    @Test
    void assessAttemptShouldReturnFailureForIncorrectAnswer() {
        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(
            "id", Language.English, "Fill in the ___", "hint", "blank", defaultSentenceId, List.of()
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
            defaultSentenceId,
            List.of()
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
            defaultSentenceId,
            List.of()
        );

        var data = originalQuestion.getQuestionTypeSpecificData();
        
        var newQuestion = FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
            originalQuestion.getID(),
            originalQuestion.getLanguage(),
            originalQuestion.getSentenceID(),
            Collections.emptyList(),
            data
        );
        
        assertEquals(originalQuestion.getID(), newQuestion.getID());
        assertEquals(originalQuestion.getLanguage(), newQuestion.getLanguage());
        assertEquals(originalQuestion.questionText, newQuestion.questionText);
        assertEquals(originalQuestion.hint, newQuestion.hint);
        assertEquals(originalQuestion.answer, newQuestion.answer);
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
            Collections.emptyList(),
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
                        Collections.emptyList(),
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
                        Collections.emptyList(),
                        incompleteData
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenSentenceIdIsInvalid(String sentenceId) {
        assertThrows(IllegalArgumentException.class, () ->
            new FillInTheBlanksQuestion("id", Language.English, "Fill in the ___", "hint", "answer", sentenceId, List.of())
        );
    }

    @Test
    void createFromQuestionTypeSpecificDataWithSourceWordExplanationParameter() {
        var wordExplanations = List.of(
            new WordExplanation(0, "Fill", List.of("verb"), "To complete something"),
            new WordExplanation(8, "the", List.of("article"), "Definite article")
        );
        
        Map<String, Object> data = Map.of(
            "questionText", "Fill in the ___",
            "answer", "blank",
            "hint", "test hint"
        );

        var newQuestion = FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
            "test-id",
            Language.English,
            defaultSentenceId,
            wordExplanations,
            data
        );

        assertEquals("Fill in the ___", newQuestion.questionText);
        assertEquals("test hint", newQuestion.hint);
        assertEquals("blank", newQuestion.answer);
        assertEquals(wordExplanations, newQuestion.getSourceWordExplanations());
    }
}
