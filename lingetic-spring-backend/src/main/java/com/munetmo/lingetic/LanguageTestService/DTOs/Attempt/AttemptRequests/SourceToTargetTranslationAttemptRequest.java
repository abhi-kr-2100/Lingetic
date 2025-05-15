package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.JsonNode;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public final class SourceToTargetTranslationAttemptRequest implements AttemptRequest {
    private final String questionID;
    private final String userResponse;

    public SourceToTargetTranslationAttemptRequest(String questionID, String userResponse) {
        if (questionID.isBlank()) {
            throw new IllegalArgumentException("questionID must not be blank");
        }

        this.questionID = questionID;
        this.userResponse = userResponse;
    }

    @Override
    public String getQuestionID() {
        return questionID;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.SourceToTargetTranslation;
    }

    public String getUserResponse() {
        return userResponse;
    }

    static SourceToTargetTranslationAttemptRequest fromJsonNode(JsonNode node) {
        if (!node.has("questionID")) {
            throw new IllegalArgumentException("SourceToTargetTranslationAttemptRequest must have a questionID");
        }

        if (!node.has("userResponse")) {
            throw new IllegalArgumentException("SourceToTargetTranslationAttemptRequest must have a userResponse");
        }

        return new SourceToTargetTranslationAttemptRequest(
            node.get("questionID").asText(),
            node.get("userResponse").asText()
        );
    }
}
