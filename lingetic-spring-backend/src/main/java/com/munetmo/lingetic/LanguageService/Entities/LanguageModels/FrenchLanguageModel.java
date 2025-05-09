package com.munetmo.lingetic.LanguageService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.Token;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LatinScriptLanguageModelHelper;
import java.util.List;
import java.util.Locale;

public final class FrenchLanguageModel implements LanguageModel {
    private final LatinScriptLanguageModelHelper helper;

    public FrenchLanguageModel() {
        this.helper = new LatinScriptLanguageModelHelper(Locale.FRANCE);
    }

    @Override
    public Language getLanguage() {
        return Language.French;
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
