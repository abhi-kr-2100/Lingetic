package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.infra.Deserializers.AttemptRequestDeserializer;

@JsonDeserialize(using = AttemptRequestDeserializer.class)
public sealed interface AttemptRequest permits FillInTheBlanksAttemptRequest, TranslationAttemptRequest {
    QuestionType getQuestionType();
    String getSentenceID();

    static AttemptRequest fromJsonNode(JsonNode node) {
        if (!node.has("questionType")) {
            throw new IllegalArgumentException("AttemptRequest must have a questionType");
        }

        var typeAsStr = node.get("questionType").asText();
        var type = QuestionType.valueOf(typeAsStr);

        return switch (type) {
            case FillInTheBlanks -> FillInTheBlanksAttemptRequest.fromJsonNode(node);
            case Translation -> TranslationAttemptRequest.fromJsonNode(node);
        };
    }
}
