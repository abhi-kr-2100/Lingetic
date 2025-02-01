package com.munetmo.lingetic.LanguageTestService.Entities.UserResponses;

public final class FillInTheBlanksUserResponse implements UserResponse {
    private static final String type = "FillInTheBlanks";
    private final String answer;

    public FillInTheBlanksUserResponse(String answer) {
        this.answer = answer;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getAnswer() {
        return answer;
    }
}
