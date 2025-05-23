package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttemptRequestTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldThrowExceptionWhenQuestionTypeMissing() throws JsonProcessingException {
        var json = """
            {
                "questionID": "123"
            }
            """;
        var node = objectMapper.readTree(json);

        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> AttemptRequest.fromJsonNode(node)
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("questionType"));
    }

    @Test
    void shouldThrowExceptionForInvalidQuestionType() throws JsonProcessingException {
        var json = """
            {
                "questionType": "ExampleInvalidType",
                "questionID": "123"
            }
            """;
        var node = objectMapper.readTree(json);
        
        var exception = assertThrows(IllegalArgumentException.class, () -> AttemptRequest.fromJsonNode(node));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("ExampleInvalidType"));
    }

    @Test
    void shouldCreateFillInTheBlanksRequestWhenTypeMatches() throws JsonProcessingException {
        var json = """
            {
                "questionType": "FillInTheBlanks",
                "questionID": "123",
                "userResponse": "test answer"
            }
            """;
        var node = objectMapper.readTree(json);
        
        var request = AttemptRequest.fromJsonNode(node);

        assertInstanceOf(FillInTheBlanksAttemptRequest.class, request);

        var fillInBlanksRequest = (FillInTheBlanksAttemptRequest) request;
        assertEquals("123", fillInBlanksRequest.getQuestionID());
        assertEquals("test answer", fillInBlanksRequest.getUserResponse());
        assertEquals(QuestionType.FillInTheBlanks, fillInBlanksRequest.getQuestionType());
    }
}
