package com.munetmo.lingetic.LanguageTestService.Entities;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SentenceReviewTest {
    @Test
    void shouldCreateSentenceReviewWithValidParameters() {
        // Given
        var id = "test-id";
        var sentenceID = "test-sentence-id";
        var userID = "test-user-id";
        var language = Language.English;

        // When
        var review = new SentenceReview(id, sentenceID, userID, language);

        // Then
        assertEquals(id, review.id);
        assertEquals(sentenceID, review.sentenceID);
        assertEquals(userID, review.userID);
        assertEquals(language, review.language);
    }

    @Test
    void shouldThrowExceptionForBlankId() {
        // Given
        var id = " ";
        var sentenceID = "test-sentence-id";
        var userID = "test-user-id";
        var language = Language.English;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new SentenceReview(id, sentenceID, userID, language));
    }

    @Test
    void shouldThrowExceptionForBlankSentenceID() {
        // Given
        var id = "test-id";
        var sentenceID = " ";
        var userID = "test-user-id";
        var language = Language.English;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new SentenceReview(id, sentenceID, userID, language));
    }

    @Test
    void shouldThrowExceptionForBlankUserID() {
        // Given
        var id = "test-id";
        var sentenceID = "test-sentence-id";
        var userID = " ";
        var language = Language.English;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new SentenceReview(id, sentenceID, userID, language));
    }

    @Test
    void shouldRejectReviewWithInvalidQualityScore() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.review(-1));
        assertThrows(IllegalArgumentException.class, () -> review.review(6));
    }

    @Test
    void shouldRejectSettingRepetitionsBelowZero() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.setRepetitions(-1));
    }

    @Test
    void shouldRejectSettingRepetitionsAboveMaxValue() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.setRepetitions(SentenceReview.MAX_REPETITIONS_VALUE + 1));
    }

    @Test
    void shouldRejectSettingEaseFactorBelowMinValue() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.setEaseFactor(1));
    }

    @Test
    void shouldAllowSettingEaseFactorSlightlyBelowMinValue() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.setEaseFactor(1.2);

        // Then
        assertEquals(1.3, review.getEaseFactor());
    }

    @Test
    void shouldRejectSettingEaseFactorAboveMaxValue() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.setEaseFactor(SentenceReview.MAX_EASE_FACTOR_VALUE + 0.9));
    }

    @Test
    void shouldAllowSettingEaseFactorSlightlyAboveMaxValue() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.setEaseFactor(SentenceReview.MAX_EASE_FACTOR_VALUE + 0.1);

        // Then
        assertEquals(SentenceReview.MAX_EASE_FACTOR_VALUE, review.getEaseFactor());
    }

    @Test
    void shouldRejectSettingIntervalBelowZero() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.setInterval(-1));
    }

    @Test
    void shouldRejectSettingIntervalAboveMaxValue() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.setInterval(SentenceReview.MAX_INTERVAL_VALUE + 1));
    }

    @Test
    void shouldRejectSettingNextReviewInstantTooFarInFuture() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> review.setNextReviewInstant(Instant.now().plus(SentenceReview.MAX_REVIEW_INSTANT_DAYS + 1, ChronoUnit.DAYS)));
    }

    @Test
    void shouldSetNextReviewInstant() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);
        var now = Instant.now();

        // When
        review.setNextReviewInstant(now.plus(1, ChronoUnit.DAYS));

        // Then
        assertEquals(now.plus(1, ChronoUnit.DAYS), review.getNextReviewInstant());
    }

    @Test
    void shouldSetRepetitions() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.setRepetitions(1);

        // Then
        assertEquals(1, review.getRepetitions());
    }

    @Test
    void shouldSetEaseFactor() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.setEaseFactor(1.3);

        // Then
        assertEquals(1.3, review.getEaseFactor());
    }

    @Test
    void shouldSetInterval() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.setInterval(1);

        // Then
        assertEquals(1, review.getInterval());
    }

    @Test
    void shouldReviewWithQuality1() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.review(1);

        // Then
        assertEquals(0, review.getRepetitions());
        assertEquals(0, review.getInterval());
        assertEquals(1.96, review.getEaseFactor(), 0.01);
    }

    @Test
    void shouldReviewWithQuality2() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.review(2);

        // Then
        assertEquals(0, review.getRepetitions());
        assertEquals(0, review.getInterval());
        assertEquals(2.18, review.getEaseFactor(), 0.01);
    }

    @Test
    void shouldReviewWithQuality3() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.review(3);

        // Then
        assertEquals(1, review.getRepetitions());
        assertEquals(1, review.getInterval());
        assertEquals(2.36, review.getEaseFactor(), 0.01);
    }

    @Test
    void shouldReviewWithQuality4() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.review(4);

        // Then
        assertEquals(1, review.getRepetitions());
        assertEquals(1, review.getInterval());
        assertEquals(2.5, review.getEaseFactor(), 0.01);
    }

    @Test
    void shouldReviewWithQuality5() {
        // Given
        var review = new SentenceReview("test-id", "test-sentence-id", "test-user-id", Language.English);

        // When
        review.review(5);

        // Then
        assertEquals(1, review.getRepetitions());
        assertEquals(1, review.getInterval());
        assertEquals(2.6, review.getEaseFactor(), 0.01);
    }
}