package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.infra.Deserializers.AttemptRequestDeserializer;

import java.util.InputMismatchException;

@JsonDeserialize(using = AttemptRequestDeserializer.class)
public final class SourceToTargetTranslationAttemptRequest implements AttemptRequest {
    private static final QuestionType questionType = QuestionType.SourceToTargetTranslation;
    private final String sentenceID;
    private final String userResponse;

    public SourceToTargetTranslationAttemptRequest(String sentenceID, String userResponse) {
        if (sentenceID.isBlank()) {
            throw new IllegalArgumentException("sentenceID cannot be blank.");
        }

        this.sentenceID = sentenceID;
        this.userResponse = userResponse;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    public static SourceToTargetTranslationAttemptRequest fromJsonNode(JsonNode node) {
        var sentenceID = node.get("sentenceID");
        if (sentenceID == null) {
            throw new InputMismatchException("sentenceID is required.");
        }

        var userResponse = node.get("userResponse");
        if (userResponse == null) {
            throw new InputMismatchException("userResponse is required.");
        }

        return new SourceToTargetTranslationAttemptRequest(sentenceID.asText(), userResponse.asText());
    }

    @Override
    public String getSentenceID() {
        return sentenceID;
    }

    public String getUserResponse() {
        return userResponse;
    }
}
