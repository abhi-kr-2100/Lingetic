package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;

public sealed interface Question permits FillInTheBlanksQuestion {
    String getID();
    QuestionType getQuestionType();
    AttemptResponse assessAttempt(AttemptRequest request);
}
