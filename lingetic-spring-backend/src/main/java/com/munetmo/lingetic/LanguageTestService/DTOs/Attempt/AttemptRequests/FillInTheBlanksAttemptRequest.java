package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.infra.Deserializers.AttemptRequestDeserializer;

@JsonDeserialize(using = AttemptRequestDeserializer.class)
public final class FillInTheBlanksAttemptRequest implements AttemptRequest {
    private static final QuestionType type = QuestionType.FillInTheBlanks;
    private final String questionID;
    public final String userResponse;

    public FillInTheBlanksAttemptRequest(String questionID, String userResponse) {
        this.questionID = questionID;
        this.userResponse = userResponse;
    }

    @Override
    public QuestionType getType() {
        return type;
    }

    @Override
    public String getQuestionID() {
        return questionID;
    }
}
