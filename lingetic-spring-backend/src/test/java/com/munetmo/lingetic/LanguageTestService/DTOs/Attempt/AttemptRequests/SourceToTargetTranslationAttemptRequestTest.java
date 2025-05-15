package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SourceToTargetTranslationAttemptRequestTest {
    @Test
    void shouldCreateRequest() {
        var request = new SourceToTargetTranslationAttemptRequest("q123", "Jag heter David.");

        assertEquals("q123", request.getQuestionID());
        assertEquals("Jag heter David.", request.getUserResponse());
        assertEquals(QuestionType.SourceToTargetTranslation, request.getQuestionType());
    }

    @Test
    void shouldThrowExceptionForBlankQuestionId() {
        assertThrows(IllegalArgumentException.class,
            () -> new SourceToTargetTranslationAttemptRequest("", "Jag heter David."));
    }

    @Test
    void shouldNotThrowExceptionForBlankUserResponse() {
        assertDoesNotThrow(() -> new SourceToTargetTranslationAttemptRequest("q123", ""));
    }

    @Test
    void shouldCreateFromJsonNode() throws Exception {
        var objectMapper = new ObjectMapper();
        var json = """
            {
                "questionType": "SourceToTargetTranslation",
                "questionID": "123",
                "userResponse": "test answer"
            }
            """;
        var node = objectMapper.readTree(json);
        var request = AttemptRequest.fromJsonNode(node);

        assertInstanceOf(SourceToTargetTranslationAttemptRequest.class, request);

        var sourceToTargetTranslationRequest = (SourceToTargetTranslationAttemptRequest) request;
        assertEquals("123", sourceToTargetTranslationRequest.getQuestionID());
        assertEquals("test answer", sourceToTargetTranslationRequest.getUserResponse());
        assertEquals(QuestionType.SourceToTargetTranslation, sourceToTargetTranslationRequest.getQuestionType());
    }

    @Test
    void fromJsonNodeShouldThrowExceptionForMissingQuestionId() throws Exception {
        var objectMapper = new ObjectMapper();
        var json = """
            {
                "questionType": "SourceToTargetTranslation",
                "userResponse": "test answer"
            }
            """;
        var node = objectMapper.readTree(json);
        assertThrows(
            IllegalArgumentException.class,
            () -> AttemptRequest.fromJsonNode(node)
        );
    }

    @Test
    void fromJsonNodeShouldThrowExceptionForMissingUserResponse() throws Exception {
        var objectMapper = new ObjectMapper();
        var json = """
            {
                "questionType": "SourceToTargetTranslation",
                "questionID": "123"
            }
            """;
        var node = objectMapper.readTree(json);
        assertThrows(
            IllegalArgumentException.class,
            () -> AttemptRequest.fromJsonNode(node)
        );
    }
}
