package com.munetmo.lingetic.LanguageTestService.infra.HTTP;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import io.jsonwebtoken.Claims;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;

@RestController
@RequestMapping("/language-test-service")
public class LanguageTestServiceController {
    @Autowired
    private TakeRegularTestUseCase takeRegularTestUseCase;

    @Autowired
    private AttemptQuestionUseCase attemptQuestionUseCase;

    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(
            @RequestParam String language,
            @AuthenticationPrincipal Claims user) {
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language parameter cannot be null or empty");
        }

        Language languageEnum = Language.valueOf(language);
        var questions = takeRegularTestUseCase.execute(user.getSubject(), languageEnum);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/questions/attempt")
    public ResponseEntity<?> attemptQuestion(@RequestBody AttemptRequest request,
            @AuthenticationPrincipal Claims user) {
        if (request == null) {
            throw new IllegalArgumentException("Attempt request cannot be null");
        }

        var response = attemptQuestionUseCase.execute(user.getSubject(), request);
        return ResponseEntity.ok(response);
    }
}
