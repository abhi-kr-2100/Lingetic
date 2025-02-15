package com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels;

public final class DummyLanguageModel implements LanguageModel {
    @Override
    public String getLanguage() {
        return "dummy";
    }

    @Override
    public boolean areEquivalent(String s1, String s2) {
        return s1.equals(s2);
    }
}
