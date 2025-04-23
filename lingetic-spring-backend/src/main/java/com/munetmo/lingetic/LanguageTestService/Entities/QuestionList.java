package com.munetmo.lingetic.LanguageTestService.Entities;

import com.munetmo.lingetic.LanguageService.Entities.Language;

public class QuestionList {
    private final String id;
    private final String name;
    private final Language language;

    public QuestionList(String id, String name, Language language) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        this.id = id;
        this.name = name;
        this.language = language;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Language getLanguage() {
        return language;
    }
}
