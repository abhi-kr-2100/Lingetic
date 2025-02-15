package com.munetmo.lingetic.LanguageTestService.infra.HTTP;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;

@CrossOrigin(origins = "${spring.web.cors.allowed-origins}")
@RestController
@RequestMapping("/questions")
public class QuestionsController {
    @Autowired
    private TakeRegularTestUseCase takeRegularTestUseCase;

    @Autowired
    private AttemptQuestionUseCase attemptQuestionUseCase;

    @GetMapping
    public ResponseEntity<?> getQuestions(@RequestParam String language) {
        if (language == null || language.isBlank()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Language parameter cannot be null or empty");
        }

        Language languageEnum;
        try {
            languageEnum = Language.valueOf(language);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid language: " + language);
        }

        var questions = takeRegularTestUseCase.execute(languageEnum);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/attempt")
    public ResponseEntity<?> attemptQuestion(@RequestBody AttemptRequest request) {
        if (request == null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Attempt request cannot be null");
        }

        try {
            var response = attemptQuestionUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (QuestionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Question not found");
        }
    }
}
