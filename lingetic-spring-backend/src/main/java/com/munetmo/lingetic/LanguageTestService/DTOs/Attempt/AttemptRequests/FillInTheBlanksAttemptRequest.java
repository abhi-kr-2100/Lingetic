package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.infra.Deserializers.AttemptRequestDeserializer;

@JsonDeserialize(using = AttemptRequestDeserializer.class)
public record FillInTheBlanksAttemptRequest(String questionID, String userResponse) implements AttemptRequest {
    private static final QuestionType questionType = QuestionType.FillInTheBlanks;

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    public static FillInTheBlanksAttemptRequest fromJsonNode(JsonNode node) {
        var questionID = node.get("questionID").asText();
        var userResponse = node.get("userResponse").asText();
        return new FillInTheBlanksAttemptRequest(questionID, userResponse);
    }
}
