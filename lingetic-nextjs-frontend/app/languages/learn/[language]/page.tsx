"use client";

import { useRouter, useParams } from "next/navigation";
import { useRef } from "react";

import FillInTheBlanks from "@/app/components/questions/FillInTheBlanks/FillInTheBlanks";
import useQuestions from "./useQuestions";
import assert from "@/utilities/assert";
import type { FillInTheBlanksQuestion, Question } from "@/utilities/api-types";
import QuestionProps from "@/app/components/questions/QuestionProps";
import { RedirectToSignIn, SignedIn, SignedOut } from "@clerk/nextjs";

type LearnPageParams = {
  language: string;
};

export default function LearnPage() {
  return (
    <>
      <SignedIn>
        <LearnPageComponent />
      </SignedIn>
      <SignedOut>
        <RedirectToSignIn />
      </SignedOut>
    </>
  );
}

// exported to allow unit testing; not meant to be used elsewhere
export function LearnPageComponent() {
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

const renderQuestion = (question: Question, onAnswerSubmit?: () => void) => {
  validateQuestionOrDie(question);

  const Component = questionTypeToComponentMap[question.questionType];
  return <Component question={question} onAnswerSubmit={onAnswerSubmit} />;
};

const validateQuestionOrDie = (question: any) => {
  assert(question != null, "question is null or undefined");
  assert(
    question.questionType != null,
    "question.questionType is null or undefined"
  );
  assert(
    Object.hasOwn(questionTypeToComponentMap, question.questionType),
    "Invalid question type"
  );
};
