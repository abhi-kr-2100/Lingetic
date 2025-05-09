package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;

import java.util.List;
import java.util.Map;

public sealed interface LanguageModel permits EnglishLanguageModel, FrenchLanguageModel, TurkishLanguageModel,
        SwedishLanguageModel, JapaneseLanguageModel, JapaneseModifiedHepburnLanguageModel {
    Language getLanguage();

    boolean areEquivalent(String s1, String s2);

    List<Token> tokenize(String sentence);

    String combineTokens(List<Token> tokens);

    static Map<Language, LanguageModel> languageModels = Map.of(
            Language.English, new EnglishLanguageModel(),
            Language.French, new FrenchLanguageModel(),
            Language.Turkish, new TurkishLanguageModel(),
            Language.Swedish, new SwedishLanguageModel(),
            Language.Japanese, new JapaneseLanguageModel(),
            Language.JapaneseModifiedHepburn, new JapaneseModifiedHepburnLanguageModel()
    );

    static LanguageModel getLanguageModel(Language language) {
        if (language == Language.DummyLanguage) {
            throw new IllegalArgumentException("DummyLanguage is not a real language");
        }

        if (!languageModels.containsKey(language)) {
            throw new IllegalStateException("Language model for " + language + " not found");
        }

        return languageModels.get(language);
    }
}
