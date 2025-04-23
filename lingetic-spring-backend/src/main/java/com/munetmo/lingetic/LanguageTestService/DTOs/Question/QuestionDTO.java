package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.lib.HashUtils;

public sealed interface QuestionDTO permits FillInTheBlanksQuestionDTO {
    String getID();
    QuestionType getQuestionType();
    Language getLanguage();

    static QuestionDTO fromQuestion(Question question) {
        return switch (question.getQuestionType()) {
            case FillInTheBlanks -> {
                var typedQuestion = (FillInTheBlanksQuestion)question;
                var fullText = typedQuestion.questionText.replaceAll("_+", typedQuestion.answer);
                var fullTextDigest = HashUtils.sha1(String.format("%s_%s", fullText.trim(), typedQuestion.getLanguage().toString()));

                yield new FillInTheBlanksQuestionDTO(
                    typedQuestion.getID(),
                    typedQuestion.getLanguage(),
                    typedQuestion.questionText,
                    fullTextDigest,
                    typedQuestion.hint
                );
            }
        };
    }
}
