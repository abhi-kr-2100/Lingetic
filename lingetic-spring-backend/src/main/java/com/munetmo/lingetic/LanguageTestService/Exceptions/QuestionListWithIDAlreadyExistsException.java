package com.munetmo.lingetic.LanguageTestService.Exceptions;

public class QuestionListWithIDAlreadyExistsException extends RuntimeException {
    public QuestionListWithIDAlreadyExistsException(String message) {
        super(message);
    }
}
