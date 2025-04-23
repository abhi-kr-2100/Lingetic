package com.munetmo.lingetic.LanguageService.infra.HTTP;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/language-service")
@ConditionalOnProperty(name = "app.environment", havingValue = "development")
public class LanguageServiceController {
    @GetMapping
    public String hello() {
        return "Hello";
    }
}
