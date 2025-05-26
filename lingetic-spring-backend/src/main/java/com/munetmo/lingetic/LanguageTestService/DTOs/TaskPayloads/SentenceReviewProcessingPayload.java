package com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;

public record SentenceReviewProcessingPayload(String userId, String sentenceId, AttemptStatus status) {
    public SentenceReviewProcessingPayload {
        if (userId.isBlank()) {
            throw new IllegalArgumentException("User ID blank");
        }
        if (sentenceId.isBlank()) {
            throw new IllegalArgumentException("Sentence ID blank");
        }
    }
}
