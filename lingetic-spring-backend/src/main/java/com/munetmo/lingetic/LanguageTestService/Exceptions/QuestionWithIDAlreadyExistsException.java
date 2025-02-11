package com.munetmo.lingetic.LanguageTestService.Exceptions;

public class QuestionWithIDAlreadyExistsException extends RuntimeException {
    public QuestionWithIDAlreadyExistsException(String message) {
        super(message);
    }
}
