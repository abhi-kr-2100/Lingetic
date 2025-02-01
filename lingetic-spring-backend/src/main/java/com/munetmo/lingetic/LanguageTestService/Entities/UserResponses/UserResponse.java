package com.munetmo.lingetic.LanguageTestService.Entities.UserResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public sealed interface UserResponse permits FillInTheBlanksUserResponse {
    public QuestionType getType();
}
