package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageService.Entities.Language;

import java.util.Map;

public sealed interface Question permits FillInTheBlanksQuestion {
    String getID();
    QuestionType getQuestionType();
    Language getLanguage();
    String getSentenceID();

    Map<String, Object> getQuestionTypeSpecificData();
    static Question createFromQuestionTypeSpecificData(String id, Language language, String sentenceId, QuestionType questionType, Map<String, Object> data) {
        return switch (questionType) {
            case FillInTheBlanks -> FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(id, language, sentenceId, data);
        };
    }

    AttemptResponse assessAttempt(AttemptRequest request);
}
