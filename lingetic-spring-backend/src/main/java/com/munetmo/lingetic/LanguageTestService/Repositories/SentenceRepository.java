package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;

public interface SentenceRepository {
    void deleteAllSentences();
    void addSentence(Sentence sentence);
}