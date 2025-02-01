package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AttemptRequestTest {

    @Test
    void shouldCreateValidAttemptRequest() {
        AttemptRequest request = new AttemptRequest("q123", "answer");
        assertEquals("q123", request.questionID());
        assertEquals("answer", request.userResponse());
    }

    @Test
    void shouldThrowOnNullQuestionID() {
        assertThrows(NullPointerException.class, () -> new AttemptRequest(null, "answer"));
    }

    @Test
    void shouldThrowOnBlankQuestionID() {
        assertThrows(IllegalArgumentException.class, () -> new AttemptRequest("", "answer"));
        assertThrows(IllegalArgumentException.class, () -> new AttemptRequest("  ", "answer"));
    }

    @Test
    void shouldThrowOnNullUserResponse() {
        assertThrows(NullPointerException.class, () -> new AttemptRequest("q123", null));
    }
}
