package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.*;

public class AttemptQuestionUseCase {
    public AttemptResponse execute(AttemptRequest request) {
        return new AttemptResponse("success", "Great job!", null);
    }
}
