package com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;

public record QuestionReviewProcessingPayload(String userId, String questionId, AttemptStatus status) {
    public QuestionReviewProcessingPayload {
        if (userId.isBlank()) {
            throw new IllegalArgumentException("User ID blank");
        }
        if (questionId.isBlank()) {
            throw new IllegalArgumentException("Question ID blank");
        }
    }
}
