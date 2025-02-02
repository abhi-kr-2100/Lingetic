"use client";

import { useRouter, useParams } from "next/navigation";
import { useRef } from "react";

import FillInTheBlanks from "@/app/components/questions/FillInTheBlanks/FillInTheBlanks";
import useQuestions from "./useQuestions";
import assert from "@/utilities/assert";
import type { FillInTheBlanksQuestion, Question } from "@/utilities/api-types";
import QuestionProps from "@/app/components/questions/QuestionProps";

type LearnPageParams = {
  language: string;
};

export default function LearnPage() {
  const { language } = useParams<LearnPageParams>();
  assert(language?.trim()?.length > 0, "language is required");

  const router = useRouter();
  const nextButtonRef = useRef<HTMLButtonElement>(null);

  const result = useQuestions({
    language,
    onFinish: () => router.push("/languages"),
  });

  if (result.isLoading) {
    return (
      <div className="container mx-auto p-4">
        <p className="text-skin-base">Loading questions...</p>
      </div>
    );
  }

  if (result.isError) {
    return (
      <div className="container mx-auto p-4">
        <p className="text-red-500">
          Failed to load questions. Please try again later.
        </p>
      </div>
    );
  }

  if (!result.hasQuestions) {
    return (
      <div className="container mx-auto p-4">
        <p className="text-skin-base">
          No questions available for this language.
        </p>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-skin-base text-3xl font-bold mb-6">
        Learning {language}
      </h1>
      {renderQuestion(result.currentQuestion, () =>
        nextButtonRef.current?.focus()
      )}
      <button
        ref={nextButtonRef}
        type="button"
        onClick={result.onNext}
        className="mt-4 bg-skin-button-primary text-skin-inverted px-4 py-2 rounded transition-colors"
      >
        {result.isLastQuestion ? "Finish" : "Next"}
      </button>
    </div>
  );
}

const questionTypeToComponentMap = {
  FillInTheBlanks: (props: QuestionProps) => (
    <FillInTheBlanks
      question={props.question as FillInTheBlanksQuestion}
      onAnswerSubmit={props.onAnswerSubmit}
    />
  ),
};

const renderQuestion = (question: Question, onAnswerSubmit: () => void) => {
  assert(
    questionTypeToComponentMap.hasOwnProperty(question.questionType),
    "Invalid question type"
  );

  const Component = questionTypeToComponentMap[question.questionType];
  return <Component question={question} onAnswerSubmit={onAnswerSubmit} />;
};
