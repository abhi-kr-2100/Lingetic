package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
    String getID();
    QuestionType getQuestionType();
    Language getLanguage();

    static QuestionDTO fromQuestion(Question question) {
        return switch (question.getQuestionType()) {
            case FillInTheBlanks -> {
                var typedQuestion = (FillInTheBlanksQuestion)question;
                yield new FillInTheBlanksQuestionDTO(
                    typedQuestion.getID(),
                    typedQuestion.getLanguage(),
                    typedQuestion.questionText,
                    typedQuestion.hint
                );
            }
        };
    }
}
