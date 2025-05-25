package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
// Remove SourceToTargetTranslation import

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
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
        };
    }
}
