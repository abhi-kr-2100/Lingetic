package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
    String getID();
    QuestionType getQuestionType();

    static QuestionDTO fromQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("question cannot be null.");
        }

        return switch (question.getQuestionType()) {
            case FillInTheBlanks -> {
                var typedQuestion = (FillInTheBlanksQuestion)question;
                yield new FillInTheBlanksQuestionDTO(typedQuestion.getID(), typedQuestion.questionText, typedQuestion.hint);
            }
        };
    }
}
