package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.lib.Utilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public sealed interface LanguageModel permits EnglishLanguageModel, FrenchLanguageModel, TurkishLanguageModel {
    Language getLanguage();

    boolean areEquivalent(String s1, String s2);

    List<Token> tokenize(String sentence);

    static final Map<Language, LanguageModel> languageModelInstances = Map.of(
            Language.English, new EnglishLanguageModel(),
            Language.French, new FrenchLanguageModel(),
            Language.Turkish, new TurkishLanguageModel());

    static LanguageModel getLanguageModel(Language language) {
        Utilities.assert_(languageModelInstances.containsKey(language), "Language not supported");

        if (languageModelInstances.containsKey(language)) {
            return languageModelInstances.get(language);
        }

        Utilities.assert_(false, "Unreachable code to satisfy compiler and NullAway");
        throw new IllegalStateException("Unreachable code");
    }
}
