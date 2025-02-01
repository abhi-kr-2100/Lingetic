package com.munetmo.lingetic.LanguageTestService.infra.HTTP;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.*;
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
        var questions = takeRegularTestUseCase.execute();
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/attempt")
    public ResponseEntity<AttemptResponse> attemptQuestion(@RequestBody AttemptRequest request) {
        var response = attemptQuestionUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
