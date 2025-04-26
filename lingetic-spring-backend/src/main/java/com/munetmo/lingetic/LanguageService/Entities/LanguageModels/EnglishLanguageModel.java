package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LatinScriptLanguageModelHelper;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class EnglishLanguageModel implements LanguageModel {
    private final LatinScriptLanguageModelHelper helper;

    public EnglishLanguageModel() {
        this.helper = new LatinScriptLanguageModelHelper(Locale.ENGLISH);
    }

    @Override
    public Language getLanguage() {
        return Language.English;
    }

    @Override
    public boolean areEquivalent(String s1, String s2) {
        return helper.areEquivalent(s1, s2);
    }

    @Override
    public List<Token> tokenize(String input) {
        return helper.tokenize(input);
    }
}
