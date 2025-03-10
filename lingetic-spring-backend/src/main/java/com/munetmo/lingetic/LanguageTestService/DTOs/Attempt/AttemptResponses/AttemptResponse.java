package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public sealed interface AttemptResponse permits FillInTheBlanksAttemptResponse {
    QuestionType getQuestionType();
    AttemptStatus getAttemptStatus();
}
