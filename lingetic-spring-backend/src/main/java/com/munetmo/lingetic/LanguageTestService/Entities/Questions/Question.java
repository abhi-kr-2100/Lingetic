package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;

public sealed interface Question permits FillInTheBlanksQuestion {
    public String getID();
    public QuestionType getType();
    public QuestionDTO toDTO();
}
