package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.InputMismatchException;

import static org.junit.jupiter.api.Assertions.*;

class FillInTheBlanksAttemptRequestTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void constructorShouldThrowExceptionWhenQuestionIDIsBlank() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FillInTheBlanksAttemptRequest("", "answer")
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("questionID"));
    }

    @Test
    void constructorShouldCreateAValidObjectWhenGivenValidArguments() {
        var request = new FillInTheBlanksAttemptRequest("test-id", "test-response");

        assertEquals("test-id", request.getQuestionID());
        assertEquals("test-response", request.getUserResponse());
        assertEquals(QuestionType.FillInTheBlanks, request.getQuestionType());
    }

    @Test
    void fromJsonNodeShouldThrowExceptionWhenQuestionIDIsMissing() throws JsonProcessingException {
        var json = """
            {
                "userResponse": "test answer"
            }
            """;
        var node = objectMapper.readTree(json);

        var exception = assertThrows(
            InputMismatchException.class,
            () -> FillInTheBlanksAttemptRequest.fromJsonNode(node)
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("questionID"));
    }

    @Test
    void fromJsonNodeShouldThrowExceptionWhenUserResponseIsMissing() throws JsonProcessingException {
        var json = """
            {
                "questionID": "123"
            }
            """;
        var node = objectMapper.readTree(json);

        var exception = assertThrows(
            InputMismatchException.class,
            () -> FillInTheBlanksAttemptRequest.fromJsonNode(node)
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("userResponse"));
    }

    @Test
    void shouldCreateValidRequestFromValidJson() throws JsonProcessingException {
        var json = """
            {
                "questionID": "123",
                "userResponse": "test answer"
            }
            """;
        var node = objectMapper.readTree(json);

        var request = FillInTheBlanksAttemptRequest.fromJsonNode(node);

        assertEquals("123", request.getQuestionID());
        assertEquals("test answer", request.getUserResponse());
        assertEquals(QuestionType.FillInTheBlanks, request.getQuestionType());
    }
}
