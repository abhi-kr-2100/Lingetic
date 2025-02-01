package com.munetmo.lingetic.LanguageTestService.infra.Deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

import java.io.IOException;

public class AttemptRequestDeserializer extends JsonDeserializer<AttemptRequest> {
    @Override
    public AttemptRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var mapper = (ObjectMapper)parser.getCodec();
        var root = (JsonNode)mapper.readTree(parser);

        if (!root.has("type")) {
            throw new IllegalArgumentException("AttemptRequest must have a type");
        }

        var typeAsStr = root.get("type").asText();
        var type = QuestionType.valueOf(typeAsStr);
        if (type == QuestionType.FillInTheBlanks) {
            var questionID = root.get("questionID").asText();
            var userResponse = root.get("userResponse").asText();
            return new FillInTheBlanksAttemptRequest(questionID, userResponse);
        }

        throw new IllegalArgumentException("Invalid type");
    }
}
