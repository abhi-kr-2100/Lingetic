package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.TokenType;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LatinScriptLanguageModelHelper;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.List;

public final class TurkishLanguageModel implements LanguageModel {
    @Override
    public Language getLanguage() {
        return Language.Turkish;
    }

    private final LatinScriptLanguageModelHelper helper;

    public TurkishLanguageModel() {
        this.helper = new LatinScriptLanguageModelHelper(Locale.forLanguageTag("tr-TR"));
    }

    @Override
    public boolean areEquivalent(String s1, String s2) {
        return helper.areEquivalent(s1, s2);
    }

    @Override
    public List<Token> tokenize(String input) {
        return helper.tokenize(input);
    }

    @Override
    public String combineTokens(List<Token> tokens) {
        return helper.combineTokens(tokens);
    }
}
