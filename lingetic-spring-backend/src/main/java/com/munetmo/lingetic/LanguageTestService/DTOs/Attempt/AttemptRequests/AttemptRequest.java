package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.infra.Deserializers.AttemptRequestDeserializer;

@JsonDeserialize(using = AttemptRequestDeserializer.class)
public sealed interface AttemptRequest permits FillInTheBlanksAttemptRequest {
    public QuestionType getType();
    public String getQuestionID();
}
