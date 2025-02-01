package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;
import com.munetmo.lingetic.LanguageTestService.Entities.Assessments.Assessment;
import com.munetmo.lingetic.LanguageTestService.Entities.UserResponses.UserResponse;

public sealed interface Question permits FillInTheBlanksQuestion {
    public String getID();
    public String getType();
    public QuestionDTO toDTO();
    public Assessment assess(UserResponse userResponse);
}
