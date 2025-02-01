package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AttemptResponseTest {

    @Test
    void shouldCreateAttemptResponse() {
        AttemptResponse response = new AttemptResponse("correct", "Good job!", "answer");
        assertEquals("correct", response.status());
        assertEquals("Good job!", response.comment());
        assertEquals("answer", response.answer());
    }
}
