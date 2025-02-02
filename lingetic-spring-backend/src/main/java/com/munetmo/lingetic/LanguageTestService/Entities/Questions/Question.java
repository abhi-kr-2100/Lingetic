package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

public sealed interface Question permits FillInTheBlanksQuestion {
    String getID();
    QuestionType getQuestionType();
}
