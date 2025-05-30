package com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SentenceReviewProcessingPayloadTest {

    @Test
    void shouldCreatePayloadWithValidParameters() {
        var payload = new SentenceReviewProcessingPayload("user123", "question456", AttemptStatus.Success);

        assertEquals("user123", payload.userId());
        assertEquals("question456", payload.sentenceId());
        assertEquals(AttemptStatus.Success, payload.status());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsBlank() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SentenceReviewProcessingPayload("", "question456", AttemptStatus.Success));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("User ID blank"));
    }

    @Test
    void shouldThrowExceptionWhenSentenceIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new SentenceReviewProcessingPayload("user123", "", AttemptStatus.Success));
    }
}
