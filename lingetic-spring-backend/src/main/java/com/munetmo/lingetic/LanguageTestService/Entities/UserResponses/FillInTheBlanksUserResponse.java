package com.munetmo.lingetic.LanguageTestService.Entities.UserResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public final class FillInTheBlanksUserResponse implements UserResponse {
    private static final QuestionType type = QuestionType.FillInTheBlanks;
    private final String answer;

    public FillInTheBlanksUserResponse(String answer) {
        this.answer = answer;
    }

    @Override
    public QuestionType getType() {
        return type;
    }

    public String getAnswer() {
        return answer;
    }
}
