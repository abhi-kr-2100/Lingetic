package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import java.util.List;

public interface SentenceRepository {
    void deleteAllSentences();
    void addSentence(Sentence sentence);
    Sentence getSentenceByID(String id);
    List<Sentence> getUnreviewedSentences(String userID, Language language, int limit);
}
