package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;

public class AttemptQuestionUseCase {
    private QuestionRepository questionRepository;

    public AttemptQuestionUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public AttemptResponse execute(AttemptRequest request) throws Exception {
        var question = questionRepository.getQuestionByID(request.questionID());

        if (question.getQuestionType() == QuestionType.FillInTheBlanks) {
            var typedQuestion = (FillInTheBlanksQuestion) question;
            var typedRequest = (FillInTheBlanksAttemptRequest) request;

            if (typedQuestion.answer.equals(typedRequest.userResponse())) {
                return new FillInTheBlanksAttemptResponse(AttemptStatus.Success, typedQuestion.answer);
            } else {
                return new FillInTheBlanksAttemptResponse(AttemptStatus.Failure, typedQuestion.answer);
            }
        }

        throw new IllegalArgumentException("Invalid question type");
    }
}
