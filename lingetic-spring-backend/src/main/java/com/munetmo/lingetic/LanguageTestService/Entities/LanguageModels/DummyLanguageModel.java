package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;

public final class DummyLanguageModel implements LanguageModel {
    @Override
    public Language getLanguage() {
        return Language.DummyLanguage;
    }

    @Override
    public boolean areEquivalent(String s1, String s2) {
        return s1.equals(s2);
    }
}
