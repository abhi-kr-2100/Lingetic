package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;

import java.util.List;
import java.util.Map;

public sealed interface Question permits FillInTheBlanksQuestion {
    String getID();
    QuestionType getQuestionType();
    Language getLanguage();
    String getSentenceID();
    List<WordExplanation> getSourceWordExplanations();

    Map<String, Object> getQuestionTypeSpecificData();
    static Question createFromQuestionTypeSpecificData(String id, Language language, String sentenceId, QuestionType questionType, List<WordExplanation> sourceWordExplanations, Map<String, Object> data) {
        return switch (questionType) {
            case FillInTheBlanks -> FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(id, language, sentenceId, sourceWordExplanations, data);
        };
    }

    AttemptResponse assessAttempt(AttemptRequest request);
}
