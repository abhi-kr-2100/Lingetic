package com.munetmo.lingetic.LanguageService.infra.HTTP;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LanguageModel;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/language-service")
@ConditionalOnProperty(name = "app.environment", havingValue = "development")
public class LanguageServiceController {
    @GetMapping("/tokenize")
    public ResponseEntity<List<Token>> tokenize(
            @RequestParam("language") Language language,
            @RequestParam("sentence") String sentence) {
        var model = LanguageModel.getLanguageModel(language);
        var tokens = model.tokenize(sentence);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/combine-tokens")
    public ResponseEntity<String> combineTokens(
            @RequestParam("language") Language language,
            @RequestBody List<Token> tokens) {
        var model = LanguageModel.getLanguageModel(language);
        var sentence = model.combineTokens(tokens);
        return ResponseEntity.ok(sentence);
    }
}
