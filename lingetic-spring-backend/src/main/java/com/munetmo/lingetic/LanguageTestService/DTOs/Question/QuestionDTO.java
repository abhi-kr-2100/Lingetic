package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.SourceToTargetTranslationQuestion;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO, SourceToTargetTranslationQuestionDTO {
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
            case SourceToTargetTranslation -> {
                var typedQuestion = (SourceToTargetTranslationQuestion)question;
                yield new SourceToTargetTranslationQuestionDTO(
                    typedQuestion.sourceLanguage,
                    typedQuestion.targetLanguage,
                    typedQuestion.sourceText,
                    typedQuestion.getSentenceID()
                );
            }
        };
    }
}
