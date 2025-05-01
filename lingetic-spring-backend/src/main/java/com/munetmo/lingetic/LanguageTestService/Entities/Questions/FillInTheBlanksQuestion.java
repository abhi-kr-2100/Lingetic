package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LanguageModel;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FillInTheBlanksQuestion implements Question {
    public record WordExplanation(int sequenceNumber, String word, List<String> properties, String comment) {
        public WordExplanation {
            if (sequenceNumber <= 0) {
                throw new IllegalArgumentException("Sequence number must be positive");
            }

            if (word.isBlank()) {
                throw new IllegalArgumentException("Word cannot be blank");
            }

            if (properties.stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("Properties cannot contain blank strings");
            }

            if (comment.isBlank()) {
                throw new IllegalArgumentException("Comment cannot be blank");
            }
        }
    }

    private final String id;
    private final Language language;
    private final String questionListId;

    private final static QuestionType questionType = QuestionType.FillInTheBlanks;

    public final String questionText;
    public final String hint;
    public final String answer;
    public final int difficulty;
    public final List<WordExplanation> explanation;

    public FillInTheBlanksQuestion(String id, Language language, String questionText, @Nullable String hint, String answer, int difficulty, String questionListId) {
        this(id, language, questionText, hint, answer, difficulty, questionListId, null);
    }

    public FillInTheBlanksQuestion(String id, Language language, String questionText, @Nullable String hint, String answer, int difficulty, String questionListId, @Nullable List<WordExplanation> explanation) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }

        if (questionText.isBlank()) {
            throw new IllegalArgumentException("Question text cannot be blank");
        }

        if (!questionText.matches("^[^_]*_+[^_]*$")) {
            throw new IllegalArgumentException("Question text must contain exactly one blank");
        }

        if (answer.isBlank()) {
            throw new IllegalArgumentException("Answer cannot be blank");
        }

        if (questionListId.isBlank()) {
            throw new IllegalArgumentException("Question list ID cannot be blank");
        }

        this.id = id;
        this.language = language;
        this.questionText = questionText;
        this.hint = Objects.requireNonNullElse(hint, "");
        this.answer = answer;
        this.difficulty = difficulty;
        this.questionListId = questionListId;
        this.explanation = Objects.requireNonNullElse(explanation, List.of());
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public String getQuestionListID() {
        return questionListId;
    }

    @Override
    public AttemptResponse assessAttempt(AttemptRequest request) {
        if (!(request instanceof FillInTheBlanksAttemptRequest typedRequest)) {
            throw new IllegalArgumentException("Invalid request type");
        }

        var areEquivalent = LanguageModel.getLanguageModel(language).areEquivalent(
            typedRequest.getUserResponse(),
            answer
        );

        return new FillInTheBlanksAttemptResponse(
            areEquivalent ? AttemptStatus.Success : AttemptStatus.Failure,
            answer,
            this.explanation
        );
    }

    @Override
    public int getDifficulty() {
        return difficulty;
    }

    @Override
    public Map<String, Object> getQuestionTypeSpecificData() {
        return Map.of(
            "questionText", questionText,
            "hint", hint,
            "answer", answer,
            "explanation", explanation
        );
    }

    public static FillInTheBlanksQuestion createFromQuestionTypeSpecificData(String id, Language language, int difficulty, String questionListId, Map<String, Object> data) {
        if (!data.containsKey("questionText") || !data.containsKey("answer")) {
            throw new IllegalArgumentException("Required fields 'questionText' and 'answer' must be present in data");
        }

        var questionText = (String) data.get("questionText");
        var answer = (String) data.get("answer");
        var hint = (String) data.getOrDefault("hint", "");

        if (data.containsKey("explanation") && !(data.get("explanation") instanceof List<?>)) {
            throw new IllegalArgumentException("Field 'explanation' must be a List.");
        }

        var rawExplanation = (List<?>) data.getOrDefault("explanation", List.of());
        var explanation = rawExplanation.stream().map(obj -> {
            var rawWordExp = (Map<?, ?>) obj;

            if (!rawWordExp.containsKey("sequenceNumber")) {
                throw new IllegalArgumentException("Required field 'sequenceNumber' must be present in explanation");
            } else if (!(rawWordExp.get("sequenceNumber") instanceof Integer)) {
                throw new IllegalArgumentException("Field 'sequenceNumber' must be an integer.");
            }

            if (!rawWordExp.containsKey("word")) {
                throw new IllegalArgumentException("Required field 'word' must be present in explanation");
            } else if (!(rawWordExp.get("word") instanceof String)) {
                throw new IllegalArgumentException("Field 'word' must be a string.");
            }

            if (!rawWordExp.containsKey("properties")) {
                throw new IllegalArgumentException("Required field 'properties' must be present in explanation");
            } else if (!(rawWordExp.get("properties") instanceof List<?>)) {
                throw new IllegalArgumentException("Field 'properties' must be a List.");
            } else if (((List<?>) rawWordExp.get("properties")).stream().anyMatch(prop -> !(prop instanceof String))) {
                throw new IllegalArgumentException("Field 'properties' must be a List of Strings.");
            }

            if (!rawWordExp.containsKey("comment")) {
                throw new IllegalArgumentException("Required field 'comment' must be present in explanation");
            } else if (!(rawWordExp.get("comment") instanceof String)) {
                throw new IllegalArgumentException("Field 'comment' must be a string.");
            }

            var sequenceNumber = (int) rawWordExp.get("sequenceNumber");
            var word = (String) rawWordExp.get("word");
            var properties = (List<String>) rawWordExp.get("properties");
            var comment = (String) rawWordExp.get("comment");

            return new WordExplanation(sequenceNumber, word, properties, comment);
        }).toList();

        return new FillInTheBlanksQuestion(id, language, questionText, hint, answer, difficulty, questionListId, explanation);
    }
}
