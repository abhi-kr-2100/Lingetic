package com.munetmo.lingetic.LanguageTestService.Entities.UserResponses;

public sealed interface UserResponse permits FillInTheBlanksUserResponse {
    public String getType();
}
