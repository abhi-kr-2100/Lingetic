package com.munetmo.lingetic.LanguageTestService.Entities;

import com.munetmo.lingetic.LanguageService.Entities.Language;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class QuestionReview {
    // The SM-2 algorithm doesn't define upper limits for these values. However, since computer memory is
    // finite and to prevent unreasonable values, reasonable limits have been chosen.
    private final static int MAX_REPETITIONS_VALUE = 1000;
    private final static double MAX_EASE_FACTOR_VALUE = 5.0;
    private final static int MAX_INTERVAL_VALUE = 365 * 10; // 10 years in days
    private final static int MAX_REVIEW_INSTANT_DAYS = 365 * 10; // 10 years in days

    public final String id;
    public final String questionID;
    public final String userID;  // Added field
    public final Language language;

    private int repetitions;
    private double easeFactor;
    private int interval;
    private Instant nextReviewInstant;

    public QuestionReview(String id, String questionID, String userID, Language language) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
        if (questionID.isBlank()) {
            throw new IllegalArgumentException("questionID cannot be blank");
        }
        if (userID.isBlank()) {
            throw new IllegalArgumentException("userID cannot be blank");
        }

        this.id = id;
        this.questionID = questionID;
        this.userID = userID;
        this.language = language;

        this.repetitions = 0;
        this.easeFactor = 2.5;
        this.interval = 0;
        this.nextReviewInstant = Instant.now();
    }

    public void review(int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }

        if (quality < 3) {
            repetitions = 0;
            interval = 0;
        } else {
            repetitions = Math.min(repetitions + 1, MAX_REPETITIONS_VALUE);

            if (repetitions == 1) {
                interval = 1;
            } else if (repetitions == 2) {
                interval = 6;
            } else {
                interval = Math.min((int) Math.round(interval * easeFactor), MAX_INTERVAL_VALUE);
            }
        }

        easeFactor = Math.min(
            MAX_EASE_FACTOR_VALUE,
            Math.max(1.3, easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        );

        nextReviewInstant = Instant.now().plus(interval, ChronoUnit.DAYS);
    }

    public Instant getNextReviewInstant() {
        return nextReviewInstant;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public int getInterval() {
        return interval;
    }

    public void setRepetitions(int repetitions) {
        if (repetitions < 0 || repetitions > MAX_REPETITIONS_VALUE) {
            throw new IllegalArgumentException("Repetitions must be between 0 and " + MAX_REPETITIONS_VALUE);
        }
        this.repetitions = repetitions;
    }

    public void setEaseFactor(double easeFactor) {
        if (easeFactor < 1.2 || easeFactor > MAX_EASE_FACTOR_VALUE + 0.1) {
                throw new IllegalArgumentException("Ease factor must be between 1.3 and " + MAX_EASE_FACTOR_VALUE);
        }
        if (easeFactor < 1.3) {
            easeFactor = 1.3;
        }
        if (easeFactor > MAX_EASE_FACTOR_VALUE) {
            easeFactor = MAX_EASE_FACTOR_VALUE;
        }

        this.easeFactor = easeFactor;
    }

    public void setInterval(int interval) {
        if (interval < 0 || interval > MAX_INTERVAL_VALUE) {
            throw new IllegalArgumentException("Interval must be between 0 and " + MAX_INTERVAL_VALUE + " days");
        }
        this.interval = interval;
    }

    public void setNextReviewInstant(Instant nextReviewInstant) {
        Instant maxAllowedInstant = Instant.now().plus(MAX_REVIEW_INSTANT_DAYS, ChronoUnit.DAYS);
        if (nextReviewInstant.isAfter(maxAllowedInstant)) {
            throw new IllegalArgumentException("Next review instant cannot be more than " + (MAX_REVIEW_INSTANT_DAYS / 365) + " years in the future");
        }
        this.nextReviewInstant = nextReviewInstant;
    }
}
