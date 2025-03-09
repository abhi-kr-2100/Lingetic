package com.munetmo.lingetic.LanguageTestService.Entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class QuestionReviewTest {
    private static final String TEST_USER_ID = "test-user-1";

    @Test
    void constructorShouldCreateValidInstance() {
        String id = "testId";
        String questionId = "testQuestionId";
        Language language = Language.English;
        
        var question = new QuestionReview(id, questionId, TEST_USER_ID, language);
        
        assertEquals(id, question.id);
        assertEquals(questionId, question.questionID);
        assertEquals(language, question.language);
    }

    @Test
    void constructorShouldThrowExceptionForBlankId() {
        assertThrows(IllegalArgumentException.class, 
            () -> new QuestionReview(" ", "questionId", TEST_USER_ID, Language.English));
    }

    @Test
    void constructorShouldThrowExceptionForBlankQuestionId() {
        assertThrows(IllegalArgumentException.class, 
            () -> new QuestionReview("id", "", TEST_USER_ID, Language.English));
    }

    @Test
    void constructorShouldThrowExceptionForBlankUserId() {
        assertThrows(IllegalArgumentException.class,
            () -> new QuestionReview("id", "questionId", "", Language.English));
    }

    @Test
    void lowQualityShouldSetNextReviewInstantToARecentInstant() {
        var question = new QuestionReview("id2", "qid2", TEST_USER_ID, Language.English);
        Instant before = Instant.now();

        question.review(2);

        Instant nextReview = question.getNextReviewInstant();
        Duration diff = Duration.between(before, nextReview);
        assertTrue(diff.isPositive());
        assertTrue(diff.toSeconds() <= 1);
    }

    @Test
    void highQualityShouldSetNextReviewInstantToADistantInstant() {
        var question = new QuestionReview("id3", "qid3", TEST_USER_ID, Language.English);
        Instant before = Instant.now();

        question.review(5);

        Instant nextReview = question.getNextReviewInstant();
        Duration diff = Duration.between(before, nextReview);
        assertTrue(diff.toDays() >= 1);
    }

    @Test
    void invalidQualityShouldThrowException() {
        var question = new QuestionReview("id4", "qid4", TEST_USER_ID, Language.English);

        assertThrows(IllegalArgumentException.class, () -> question.review(-1));
        assertThrows(IllegalArgumentException.class, () -> question.review(6));
    }

    @Test
    void setRepetitionsShouldSetValidValue() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);
        int newRepetitions = 500;

        question.setRepetitions(newRepetitions);

        assertEquals(newRepetitions, question.getRepetitions());
    }

    @Test
    void setRepetitionsShouldThrowExceptionForNegativeValue() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);

        assertThrows(IllegalArgumentException.class, () -> question.setRepetitions(-1));
    }

    @Test
    void setRepetitionsShouldThrowExceptionForValueAboveMax() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);

        assertThrows(IllegalArgumentException.class, () -> question.setRepetitions(1001));
    }

    @Test
    void setEaseFactorShouldSetValidValue() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);
        double newEaseFactor = 2.0;

        question.setEaseFactor(newEaseFactor);

        assertEquals(newEaseFactor, question.getEaseFactor());
    }

    @Test
    void setEaseFactorShouldAdjustValuesSlightlyBelowMinimum() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);
        double inputEaseFactor = 1.29;

        question.setEaseFactor(inputEaseFactor);

        assertEquals(1.3, question.getEaseFactor());
    }

    @Test
    void setEaseFactorShouldAdjustValueSlightlyAboveMaximum() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);
        double inputEaseFactor = 5.01;

        question.setEaseFactor(inputEaseFactor);

        assertEquals(5.0, question.getEaseFactor());
    }

    @Test
    void setEaseFactorShouldThrowExceptionForValueSignificantlyBelowMinimum() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);

        assertThrows(IllegalArgumentException.class, () -> question.setEaseFactor(1.0));
    }

    @Test
    void setEaseFactorShouldThrowExceptionForValueSignificantlyAboveMaximum() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);

        assertThrows(IllegalArgumentException.class, () -> question.setEaseFactor(15.0));
    }

    @Test
    void setIntervalShouldSetValidValue() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);
        int newInterval = 10;

        question.setInterval(newInterval);

        assertEquals(newInterval, question.getInterval());
    }

    @Test
    void setIntervalShouldThrowExceptionForNegativeValue() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);

        assertThrows(IllegalArgumentException.class, () -> question.setInterval(-1));
    }

    @Test
    void setIntervalShouldThrowExceptionForValueAboveMaximum() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);

        assertThrows(IllegalArgumentException.class, () -> question.setInterval(365 * 10 + 1));
    }

    @Test
    void setNextReviewInstantShouldSetValidValue() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);
        Instant newInstant = Instant.now().plus(5, ChronoUnit.DAYS);

        question.setNextReviewInstant(newInstant);

        assertEquals(newInstant, question.getNextReviewInstant());
    }

    @Test
    void setNextReviewInstantShouldThrowExceptionForValueTooFarInFuture() {
        var question = new QuestionReview("id1", "qid1", TEST_USER_ID, Language.English);
        Instant farFuture = Instant.now().plus(365 * 11, ChronoUnit.DAYS); // 11 years in days

        assertThrows(IllegalArgumentException.class, () -> question.setNextReviewInstant(farFuture));
    }
}
