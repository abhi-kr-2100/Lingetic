package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;

import java.util.Map;

public sealed interface LanguageModel permits EnglishLanguageModel, TurkishLanguageModel {
    Language getLanguage();
    boolean areEquivalent(String s1, String s2);

    static final Map<Language, LanguageModel> languageModelInstances = Map.of(
            Language.English, new EnglishLanguageModel(),
            Language.Turkish, new TurkishLanguageModel()
    );

    static LanguageModel getLanguageModel(Language language) {
        if (languageModelInstances.containsKey(language)) {
            return languageModelInstances.get(language);
        }

        throw new IllegalArgumentException("Language not supported");
    }
}
