package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;

public sealed interface Question permits FillInTheBlanksQuestion {
    String getID();
    QuestionType getQuestionType();
    Language getLanguage();
    AttemptResponse assessAttempt(AttemptRequest request);
}
