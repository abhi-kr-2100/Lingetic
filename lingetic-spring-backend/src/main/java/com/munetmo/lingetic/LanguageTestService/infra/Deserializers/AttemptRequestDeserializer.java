package com.munetmo.lingetic.LanguageTestService.infra.Deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;

import java.io.IOException;

public class AttemptRequestDeserializer extends JsonDeserializer<AttemptRequest> {
    @Override
    public AttemptRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var mapper = (ObjectMapper)parser.getCodec();
        var root = (JsonNode)mapper.readTree(parser);
        return AttemptRequest.fromJsonNode(root);
    }
}
