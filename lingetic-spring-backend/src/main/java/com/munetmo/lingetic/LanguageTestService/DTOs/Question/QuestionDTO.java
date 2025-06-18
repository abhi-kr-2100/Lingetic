package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.TranslationQuestion;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO, TranslationQuestionDTO {
    QuestionType getQuestionType();
    String getSentenceID();

    static QuestionDTO fromQuestion(Question question) {
        return switch (question.getQuestionType()) {
            case FillInTheBlanks -> {
                var typedQuestion = (FillInTheBlanksQuestion)question;
                yield new FillInTheBlanksQuestionDTO(
                    typedQuestion.questionText,
                    typedQuestion.hint,
                    typedQuestion.getSentenceID()
                );
            }
            case Translation -> {
                var typedQuestion = (TranslationQuestion)question;
                yield new TranslationQuestionDTO(
                    typedQuestion.translateFromLanguage,
                    typedQuestion.translateToLanguage,
                    typedQuestion.toTranslateText,
                    typedQuestion.getSentenceID()
                );
            }
        };
    }
}
