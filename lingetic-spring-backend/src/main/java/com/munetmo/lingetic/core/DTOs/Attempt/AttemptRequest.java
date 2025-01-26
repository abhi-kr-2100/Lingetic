package com.munetmo.lingetic.core.DTOs.Attempt;

import java.util.Objects;

public record AttemptRequest(String questionID, String userResponse) {
    public AttemptRequest {
        Objects.requireNonNull(questionID, "questionID must not be null");
        if (questionID.isBlank()) {
            throw new IllegalArgumentException("questionID must not be blank");
        }

        Objects.requireNonNull(userResponse, "userResponse must not be null");
    }
}
