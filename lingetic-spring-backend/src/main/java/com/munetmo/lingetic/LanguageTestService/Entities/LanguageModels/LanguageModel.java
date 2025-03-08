package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.lib.Utilities;

import java.util.Map;

public sealed interface LanguageModel permits EnglishLanguageModel, TurkishLanguageModel {
    Language getLanguage();
    boolean areEquivalent(String s1, String s2);

    static final Map<Language, LanguageModel> languageModelInstances = Map.of(
            Language.English, new EnglishLanguageModel(),
            Language.Turkish, new TurkishLanguageModel()
    );

    static LanguageModel getLanguageModel(Language language) {
        Utilities.assert_(languageModelInstances.containsKey(language), "Language not supported");

        if (languageModelInstances.containsKey(language)) {
            return languageModelInstances.get(language);
        }

        Utilities.assert_(false, "Unreachable code to satisfy compiler and NullAway");
        throw new RuntimeException("Unreachable code");
    }
}
