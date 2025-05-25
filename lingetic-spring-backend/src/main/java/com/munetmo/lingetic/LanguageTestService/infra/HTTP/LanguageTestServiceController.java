package com.munetmo.lingetic.LanguageTestService.infra.HTTP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import io.jsonwebtoken.Claims;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;

import java.util.List;

@RestController
@RequestMapping("/language-test-service")
public class LanguageTestServiceController {
    @Autowired
    private TakeRegularTestUseCase takeRegularTestUseCase;

    @Autowired
    private AttemptQuestionUseCase attemptQuestionUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(
            @RequestParam String language,
            @RequestParam(required = false) String questionListId,
            @AuthenticationPrincipal Claims user) {
        if (language == null || language.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Language parameter cannot be null or empty");
        }

        // Convert blank questionListId to null for optional filtering
        if (questionListId != null && questionListId.isBlank()) {
            questionListId = null;
        }

        Language languageEnum;
        try {
            languageEnum = Language.valueOf(language);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid language: " + language);
        }

        var questions = takeRegularTestUseCase.execute(user.getSubject(), languageEnum);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/questions/attempt")
    public ResponseEntity<?> attemptQuestion(@RequestBody AttemptRequest request,
            @AuthenticationPrincipal Claims user) {
        if (request == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Attempt request cannot be null");
        }

        try {
            var response = attemptQuestionUseCase.execute(user.getSubject(), request);
            return ResponseEntity.ok(response);
        } catch (QuestionNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Question not found for sentence: " + request.getSentenceID());
        }
    }
}
