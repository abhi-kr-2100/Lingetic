package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;

import java.util.Collections;
import java.util.Map;

public sealed interface LanguageModel permits DummyLanguageModel, EnglishLanguageModel {
    Language getLanguage();
    boolean areEquivalent(String s1, String s2);

    static final DummyLanguageModel dummyLanguageModelInstance = new DummyLanguageModel();
    static final Map<Language, LanguageModel> languageModelInstances = Collections.unmodifiableMap(Map.of(
        Language.English, new EnglishLanguageModel()
    ));

    public static LanguageModel getLanguageModel(Language language) {
        return languageModelInstances.getOrDefault(language, dummyLanguageModelInstance);
    }
}
