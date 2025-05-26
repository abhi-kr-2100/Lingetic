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
    void constructorShouldThrowExceptionWhenSentenceIDIsBlank() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FillInTheBlanksAttemptRequest("", "answer")
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("sentenceID"));
    }

    @Test
    void constructorShouldCreateAValidObjectWhenGivenValidArguments() {
        var request = new FillInTheBlanksAttemptRequest("test-id", "test-response");

        assertEquals("test-id", request.getSentenceID());
        assertEquals("test-response", request.getUserResponse());
        assertEquals(QuestionType.FillInTheBlanks, request.getQuestionType());
    }

    @Test
    void fromJsonNodeShouldThrowExceptionWhenSentenceIDIsMissing() throws JsonProcessingException {
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
        assertTrue(exception.getMessage().contains("sentenceID"));
    }

    @Test
    void fromJsonNodeShouldThrowExceptionWhenUserResponseIsMissing() throws JsonProcessingException {
        var json = """
            {
                "sentenceID": "123"
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
                "sentenceID": "123",
                "userResponse": "test answer"
            }
            """;
        var node = objectMapper.readTree(json);

        var request = FillInTheBlanksAttemptRequest.fromJsonNode(node);

        assertEquals("123", request.getSentenceID());
        assertEquals("test answer", request.getUserResponse());
        assertEquals(QuestionType.FillInTheBlanks, request.getQuestionType());
    }
}
