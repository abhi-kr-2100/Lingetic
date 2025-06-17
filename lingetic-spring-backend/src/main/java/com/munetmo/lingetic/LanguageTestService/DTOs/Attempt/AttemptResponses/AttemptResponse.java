package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;

import java.util.List;

public sealed interface AttemptResponse permits FillInTheBlanksAttemptResponse, SourceToTargetTranslationAttemptResponse {
    QuestionType getQuestionType();
    AttemptStatus getAttemptStatus();
    List<WordExplanation> getSourceWordExplanations();
}
