package com.munetmo.lingetic.LanguageTestService.Entities;

public final class QuestionList {
    private final String id;
    private final String name;

    public QuestionList(String id, String name) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        this.id = id;
        this.name = name;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }
}