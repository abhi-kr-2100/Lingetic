package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;

public record AttemptResponse(AttemptStatus status, String comment, String answer) {
}
