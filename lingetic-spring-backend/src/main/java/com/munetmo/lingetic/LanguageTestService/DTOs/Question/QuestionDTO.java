package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.SourceToTargetTranslation;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO, SourceToTargetTranslationDTO {
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
            case SourceToTargetTranslation -> {
                var typedQuestion = (SourceToTargetTranslation)question;
                yield new SourceToTargetTranslationDTO(
                    typedQuestion.getID(),
                    typedQuestion.getLanguage(),
                    typedQuestion.translation
                );
            }
        };
    }
}
