package com.munetmo.lingetic.core.DTOs.Question;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
    public String getID();

    public String getType();
}
