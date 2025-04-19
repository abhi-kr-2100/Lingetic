package com.munetmo.lingetic.HealthService.infra.HTTP;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-service")
public class HealthServiceController {
    @GetMapping("/wakeup")
    public ResponseEntity<Map<String, String>> wakeup() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
