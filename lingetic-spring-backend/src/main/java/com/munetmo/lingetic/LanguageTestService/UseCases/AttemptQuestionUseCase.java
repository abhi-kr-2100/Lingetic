package com.munetmo.lingetic.LanguageTestService.UseCases;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads.SentenceReviewProcessingPayload;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Queues.QueueNames;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
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
        var question = questionRepository.getQuestionBySentenceID(request.getSentenceID());
        var response = question.assessAttempt(request);

        var payload = new SentenceReviewProcessingPayload(
                userId,
                request.getSentenceID(),
                response.getAttemptStatus());

        var unused = taskSubmitExecutor.submit(() -> {
            taskQueue.submitTask(
                    generateTaskId(userId, request.getSentenceID()),
                    payload,
                    QueueNames.REVIEW_PROCESSING_QUEUE);
        });

        return response;
    }

    private String generateTaskId(String userId, String sentenceID) {
        // timestamp in seconds because updates to the same question by the same user
        // should be considered duplicates if they happen in quick succession
        var timestamp = Instant.now().getEpochSecond();

        return String.format("AttemptQuestionUseCase|%s|%s|%s", userId, sentenceID, timestamp);
    }
}
