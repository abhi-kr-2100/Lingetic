package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AttemptResponseTest {

    @Test
    void shouldCreateAttemptResponse() {
        AttemptResponse response = new AttemptResponse(AttemptStatus.Success, "Good job!", "answer");
        assertEquals(AttemptStatus.Success, response.status());
        assertEquals("Good job!", response.comment());
        assertEquals("answer", response.answer());
    }
}
