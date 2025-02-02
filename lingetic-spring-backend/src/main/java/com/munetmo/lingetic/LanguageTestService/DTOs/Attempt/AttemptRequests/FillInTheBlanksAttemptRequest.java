package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.infra.Deserializers.AttemptRequestDeserializer;

@JsonDeserialize(using = AttemptRequestDeserializer.class)
public final class FillInTheBlanksAttemptRequest implements AttemptRequest {
    private static final QuestionType questionType = QuestionType.FillInTheBlanks;
    private final String questionID;
    private final String userResponse;

    public FillInTheBlanksAttemptRequest(String questionID, String userResponse) {
        this.questionID = questionID;
        this.userResponse = userResponse;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    public static FillInTheBlanksAttemptRequest fromJsonNode(JsonNode node) {
        var questionID = node.get("questionID").asText();
        var userResponse = node.get("userResponse").asText();
        return new FillInTheBlanksAttemptRequest(questionID, userResponse);
    }

    @Override
    public String getQuestionID() {
        return questionID;
    }

    public String getUserResponse() {
        return userResponse;
    }
}
