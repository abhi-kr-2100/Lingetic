package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.lib.Utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public sealed interface LanguageModel permits EnglishLanguageModel, FrenchLanguageModel, TurkishLanguageModel, SwedishLanguageModel, JapaneseLanguageModel {
    Language getLanguage();

    boolean areEquivalent(String s1, String s2);

    List<Token> tokenize(String sentence);

    static LanguageModel getLanguageModel(Language language) {
        return switch (language) {
            case DummyLanguage -> throw new IllegalArgumentException("DummyLanguage is not a real language");
            case English -> new EnglishLanguageModel();
            case French -> new FrenchLanguageModel();
            case Turkish -> new TurkishLanguageModel();
            case Swedish -> new SwedishLanguageModel();
            case Japanese -> {
                try {
                    yield new JapaneseLanguageModel();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to initialize Japanese language model: " + e.getMessage(), e);
                }
            }
        };
    }
}
