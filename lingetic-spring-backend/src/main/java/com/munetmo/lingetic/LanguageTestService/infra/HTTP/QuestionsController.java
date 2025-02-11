package com.munetmo.lingetic.LanguageTestService.infra.HTTP;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;

import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;

import java.util.List;

@CrossOrigin(origins = "${spring.web.cors.allowed-origins}")
@RestController
@RequestMapping("/questions")
public class QuestionsController {
    @Autowired
    private TakeRegularTestUseCase takeRegularTestUseCase;

    @Autowired
    private AttemptQuestionUseCase attemptQuestionUseCase;

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getQuestions(@RequestParam String language) {
        var questions = takeRegularTestUseCase.execute(language);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/attempt")
    public ResponseEntity<?> attemptQuestion(@RequestBody AttemptRequest request) {
        try {
            var response = attemptQuestionUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (QuestionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Question not found");
        }
    }
}
