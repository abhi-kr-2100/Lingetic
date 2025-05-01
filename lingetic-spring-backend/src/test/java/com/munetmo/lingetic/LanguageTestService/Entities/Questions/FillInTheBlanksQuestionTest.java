package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

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

        var explanation = new ArrayList<FillInTheBlanksQuestion.WordExplanation>();
        explanation.add(new FillInTheBlanksQuestion.WordExplanation(
                1, "blank", List.of("article", "plural", "definite"), "test comment"));

        FillInTheBlanksQuestion question = new FillInTheBlanksQuestion(id, language, questionText, hint, answer, 5, defaultQuestionListId, explanation);

        assertEquals(id, question.getID());
        assertEquals(language, question.getLanguage());
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(questionText, question.questionText);
        assertEquals(hint, question.hint);
        assertEquals(answer, question.answer);
        assertEquals(5, question.difficulty);
        assertEquals(defaultQuestionListId, question.getQuestionListID());

        assertEquals(1, question.explanation.size());
        var first = explanation.getFirst();
        assertEquals(1, first.sequenceNumber());
        assertEquals("blank", first.word());
        assertEquals(List.of("article", "plural", "definite"), first.properties());
        assertEquals("test comment", first.comment());
    }

    @Test
    void constructorShouldCreateValidObjectWithoutExplanation() {
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
        assertTrue(question.explanation.isEmpty());
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

        var explanation = data.get("explanation");
        assertInstanceOf(List.class, explanation);
        var castedExplanation = (List<?>) explanation;
        assertNotNull(castedExplanation);
        assertTrue((castedExplanation.isEmpty()));
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
        assertTrue(newQuestion.explanation.isEmpty());
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
        assertTrue(newQuestion.explanation.isEmpty());
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
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenSequenceNumberIsMissing() {
        var explanationData = Map.of(
                "word", "example",
                "properties", List.of("prop1"),
                "comment", "This is a comment"
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenSequenceNumberIsNotAnInt() {
        var explanationData = Map.of(
                "sequenceNumber", "not an int",
                "word", "example",
                "properties", List.of("prop1"),
                "comment", "This is a comment"
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenWordIsMissing() {
        var explanationData = Map.of(
                "sequenceNumber", 1,
                "properties", List.of("prop1"),
                "comment", "This is a comment"
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenWordIsNotAString() {
        var explanationData = Map.of(
                "sequenceNumber", 1,
                "word", 1,
                "properties", List.of("prop1"),
                "comment", "This is a comment"
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenCommentIsMissing() {
        var explanationData = Map.of(
                "sequenceNumber", 1,
                "word", "example",
                "properties", List.of("prop1")
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenCommentIsNotAString() {
        var explanationData = Map.of(
                "sequenceNumber", 1,
                "word", "example",
                "properties", List.of("prop1"),
                "comment", 1
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenPropertiesIsMissing() {
        var explanationData = Map.of(
                "sequenceNumber", 1,
                "word", "example",
                "comment", "This is a comment"
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenPropertiesIsNotAList() {
        var explanationData = Map.of(
                "sequenceNumber", 1,
                "word", "example",
                "properties", "not a list",
                "comment", "This is a comment"
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenPropertiesIsNotAListOfStrings() {
        var explanationData = Map.of(
                "sequenceNumber", 1,
                "word", "example",
                "properties", List.of(1),
                "comment", "This is a comment"
        );

        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldHandleExplanationField() {
        var explanationList = List.of(
            Map.of(
                "sequenceNumber", 1,
                "word", "Les",
                "properties", List.of("article", "plural", "definite"),
                "comment", "Used because 'étudiants' is plural noun."
            )
        );

        var data = Map.of(
            "questionText", "Les ___ étudiants",
            "answer", "Les",
            "hint", "article",
            "explanation", explanationList
        );
        var question = FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(
            "id",
            Language.French,
            3,
            defaultQuestionListId,
            data
        );
        assertFalse(question.explanation.isEmpty());
        var first = question.explanation.get(0);
        assertEquals(1, first.sequenceNumber());
        assertEquals("Les", first.word());
        assertEquals(java.util.List.of("article", "plural", "definite"), first.properties());
        assertEquals("Used because 'étudiants' is plural noun.", first.comment());
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

    @Test
    void createFromQuestionTypeSpecificDataShouldHandleMissingExplanationField() {
        Map<String, Object> data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank"
        );

        var question = FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        assertTrue(question.explanation.isEmpty());
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenExplanationIsNotAList() {
        Map<String, Object> data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", "not a list"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Test
    void createFromQuestionTypeSpecificDataShouldThrowExceptionWhenExplanationIsMapButNotMapFromString() {
        var explanationData = Map.of(
                123, "value" // not a Map<String, Object>
        );
        var data = Map.of(
                "questionText", "Fill in the ___",
                "answer", "blank",
                "explanation", List.of(explanationData)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            FillInTheBlanksQuestion.createFromQuestionTypeSpecificData("id", Language.English, 1, "list-id", data);
        });
    }

    @Nested
    class WordExplanationTest {
        @Test
        void constructorShouldThrowExceptionForNegativeSequenceNumber() {
            assertThrows(IllegalArgumentException.class, () -> {
                new FillInTheBlanksQuestion.WordExplanation(-1, "word", Arrays.asList("prop1"), "comment");
            });
        }

        @Test
        void constructorShouldThrowExceptionForBlankWord() {
            assertThrows(IllegalArgumentException.class, () -> {
                new FillInTheBlanksQuestion.WordExplanation(1, "", Arrays.asList("prop1"), "comment");
            });
        }

        @Test
        void constructorShouldThrowExceptionForBlankProperties() {
            assertThrows(IllegalArgumentException.class, () -> {
                new FillInTheBlanksQuestion.WordExplanation(1, "word", Arrays.asList("prop1", ""), "comment");
            });
        }

        @Test
        void constructorShouldThrowExceptionForBlankComment() {
            assertThrows(IllegalArgumentException.class, () -> {
                new FillInTheBlanksQuestion.WordExplanation(1, "word", Arrays.asList("prop1"), "");
            });
        }

        @Test
        void constructorShouldCreateValidObject() {
            FillInTheBlanksQuestion.WordExplanation wordExplanation = new FillInTheBlanksQuestion.WordExplanation(1, "word", Arrays.asList("prop1"), "comment");
            assertNotNull(wordExplanation);
            assertEquals(1, wordExplanation.sequenceNumber());
            assertEquals("word", wordExplanation.word());
            assertEquals(Arrays.asList("prop1"), wordExplanation.properties());
            assertEquals("comment", wordExplanation.comment());
        }
    }
}
