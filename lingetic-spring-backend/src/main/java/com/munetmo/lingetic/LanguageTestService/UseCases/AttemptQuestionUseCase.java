package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.*;
import com.munetmo.lingetic.LanguageTestService.Entities.UserResponses.FillInTheBlanksUserResponse;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import org.apache.coyote.http11.filters.IdentityInputFilter;

public class AttemptQuestionUseCase {
    private QuestionRepository questionRepository;

    public AttemptQuestionUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public AttemptResponse execute(AttemptRequest request) throws Exception {
        var question = questionRepository.getQuestionByID(request.questionID());
        var userResponse = switch (question.getType()) {
            case "FillInTheBlanks" -> new FillInTheBlanksUserResponse(request.userResponse());
            default -> throw new Exception("Unsupported question type.");
        };

        var assessment = question.assess(userResponse);
        return assessment.toDTO();
    }
}
