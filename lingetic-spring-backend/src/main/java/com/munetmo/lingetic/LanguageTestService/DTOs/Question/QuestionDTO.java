package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
    public String getID();

    public QuestionType getQuestionType();
}
