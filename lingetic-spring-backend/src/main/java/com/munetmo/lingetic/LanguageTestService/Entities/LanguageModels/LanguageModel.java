package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import java.util.Map;

public sealed interface LanguageModel permits DummyLanguageModel, EnglishLanguageModel {
    String getLanguage();
    boolean areEquivalent(String s1, String s2);

    public static LanguageModel getLanguageModel(String language) {
        var models = Map.of(
            "english", (LanguageModel) new EnglishLanguageModel()
        );

        return models.getOrDefault(language, new DummyLanguageModel());
    }
}
