package com.munetmo.lingetic.LanguageTestService.Entities.Assessments;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public sealed interface Assessment permits FillInTheBlanksAssessment {
    public QuestionType getType();
    public AttemptStatus getStatus();
    public String getComment();
    public AttemptResponse toDTO();
}
