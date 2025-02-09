package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.infra.Deserializers.AttemptRequestDeserializer;

import java.util.InputMismatchException;

@JsonDeserialize(using = AttemptRequestDeserializer.class)
public final class FillInTheBlanksAttemptRequest implements AttemptRequest {
    private static final QuestionType questionType = QuestionType.FillInTheBlanks;
    private final String questionID;
    private final String userResponse;

    public FillInTheBlanksAttemptRequest(String questionID, String userResponse) {
        if (questionID == null || questionID.isBlank()) {
            throw new IllegalArgumentException("questionID cannot be null.");
        }
        if (userResponse == null) {
            throw new IllegalArgumentException("userResponse cannot be null.");
        }

        this.questionID = questionID;
        this.userResponse = userResponse;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    public static FillInTheBlanksAttemptRequest fromJsonNode(JsonNode node) {
        if (node == null) {
            throw new IllegalArgumentException("node cannot be null.");
        }

        var questionID = node.get("questionID");
        if (questionID == null) {
            throw new InputMismatchException("questionID is required.");
        }

        var userResponse = node.get("userResponse");
        if (userResponse == null) {
            throw new InputMismatchException("userResponse is required.");
        }

        return new FillInTheBlanksAttemptRequest(questionID.asText(), userResponse.asText());
    }

    @Override
    public String getQuestionID() {
        return questionID;
    }

    public String getUserResponse() {
        return userResponse;
    }
}
