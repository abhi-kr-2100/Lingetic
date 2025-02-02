package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
    String getID();
    QuestionType getQuestionType();

    static QuestionDTO fromQuestion(Question question) {
        switch (question.getQuestionType()) {
            case FillInTheBlanks -> {
                var typedQuestion = (FillInTheBlanksQuestion)question;
                return new FillInTheBlanksQuestionDTO(question.getID(), typedQuestion.questionText, typedQuestion.hint);
            }
            default -> throw new IllegalArgumentException("Unsupported question type");
        }
    }
}
