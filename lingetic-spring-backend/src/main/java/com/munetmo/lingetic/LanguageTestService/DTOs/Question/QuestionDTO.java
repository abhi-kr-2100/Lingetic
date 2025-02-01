package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
    public String getID();

    public String getType();
}
