package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public record FillInTheBlanksAttemptResponse(AttemptStatus attemptStatus,
                                             String correctAnswer) implements AttemptResponse {
    private static final QuestionType questionType = QuestionType.FillInTheBlanks;

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }
}
