package com.munetmo.lingetic.LanguageTestService.UseCases;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads.QuestionReviewProcessingPayload;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Queues.QueueNames;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.lib.tasks.TaskQueue;

public class AttemptQuestionUseCase {
    private final QuestionRepository questionRepository;

    private final TaskQueue taskQueue;
    private final ExecutorService taskSubmitExecutor;

    public AttemptQuestionUseCase(QuestionRepository questionRepository, TaskQueue taskQueue,
            ExecutorService taskSubmitExecutor) {
        this.questionRepository = questionRepository;
        this.taskQueue = taskQueue;
        this.taskSubmitExecutor = taskSubmitExecutor;
    }

    public AttemptResponse execute(String userId, AttemptRequest request)
            throws QuestionNotFoundException {
        var question = questionRepository.getQuestionByID(request.getQuestionID());
        var response = question.assessAttempt(request);

        var payload = new QuestionReviewProcessingPayload(
                userId,
                question.getID(),
                response.getAttemptStatus());

        taskSubmitExecutor.submit(() -> {
            taskQueue.submitTask(
                    generateTaskId(userId, question),
                    payload,
                    QueueNames.QUESTION_REVIEW_PROCESSING_QUEUE);
        });

        return response;
    }

    private String generateTaskId(String userId, Question question) {
        // timestamp in seconds because updates to the same question by the same user
        // should be considered duplicates if they happen in quick succession
        var timestamp = Instant.now().getEpochSecond();

        return String.format("AttemptQuestionUseCase|%s|%s|%s", userId, question.getID(), timestamp);
    }
}
