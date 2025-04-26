"use client";

import { useRouter, useParams } from "next/navigation";
import { useRef, useState } from "react";
import { ArrowRight, BookOpen, XCircle, Loader2 } from "lucide-react";
import useQuestions, { SuccessState } from "./useQuestions";
import assert from "@/utilities/assert";
import FillInTheBlanks from "@/app/components/questions/FillInTheBlanks/FillInTheBlanks";
import type {
  AttemptStatus,
  FillInTheBlanksQuestion,
  Question,
} from "@/utilities/api-types";
import type QuestionProps from "@/app/components/questions/QuestionProps";

export default function LearnPageComponent() {
  const params = useParams() as LearnPageParams;
  const { language } = params;
  assert(language.trim().length > 0, "language is required");

  const router = useRouter();
  const nextButtonRef = useRef<HTMLButtonElement>(null);
  const [correctAnswers, setCorrectAnswers] = useState(0);

  const result = useQuestions({
    language,
    onFinish: () => {
      assert(
        result.hasQuestions,
        "Questions were not available before finishing."
      );

      const searchParams = new URLSearchParams({
        total: (result as SuccessState).totalQuestions.toString(),
        correct: correctAnswers.toString(),
      });
      router.push(`/languages/${language}/results?${searchParams.toString()}`);
    },
  });

  if (result.isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
          <Loader2 className="h-12 w-12 text-[#2563eb] animate-spin mx-auto mb-4" />
          <p className="text-[#374151] text-lg">
            Loading your language exercises...
          </p>
        </div>
      </div>
    );
  }

  if (result.isError) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
          <XCircle className="h-12 w-12 text-[#dc2626] mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-[#374151] mb-2">Oops!</h2>
          <p className="text-[#374151] mb-6">
            {"We couldn't load your questions. Please try again later."}
          </p>
          <button
            onClick={() => {
              router.push("/languages");
            }}
            className="bg-[#2563eb] text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors"
          >
            Back to Languages
          </button>
        </div>
      </div>
    );
  }

  if (!result.hasQuestions) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
          <BookOpen className="h-12 w-12 text-[#2563eb] mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-[#374151] mb-2">
            No Questions Available
          </h2>
          <p className="text-[#374151] mb-6">
            {`We don't have any questions for ${language} yet. Please check back later.`}
          </p>
          <button
            onClick={() => {
              router.push("/languages");
            }}
            className="bg-[#2563eb] text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors"
          >
            Back to Languages
          </button>
        </div>
      </div>
    );
  }

  const afterAnswerCheck = (attemptStatus?: AttemptStatus) => {
    if (attemptStatus === "Success") {
      setCorrectAnswers((prev) => prev + 1);
    }
    nextButtonRef.current?.focus();
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white py-12 px-4">
      <div className="max-w-3xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-[#374151]">
            Learning <span className="text-[#2563eb]">{language}</span>
          </h1>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-8 mb-6">
          <RenderQuestion
            question={result.currentQuestion}
            afterAnswerCheck={afterAnswerCheck}
          />
        </div>

        <div className="flex justify-end">
          <button
            ref={nextButtonRef}
            type="button"
            onClick={result.onNext}
            className="bg-[#2563eb] text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center"
          >
            {result.isLastQuestion ? "Finish" : "Next"}
            <ArrowRight className="ml-2 h-5 w-5" />
          </button>
        </div>
      </div>
    </div>
  );
}

interface LearnPageParams {
  language: string;

  // Not used, but required by useParams
  [key: string]: string | undefined;
}

interface RenderQuestionProps {
  question: Question;
  afterAnswerCheck?: (attemptStatus?: AttemptStatus) => void;
}

const RenderQuestion = ({
  question,
  afterAnswerCheck,
}: RenderQuestionProps) => {
  validateQuestionOrDie(question);

  const Component = questionTypeToComponentMap[question.questionType];
  return <Component question={question} afterAnswerCheck={afterAnswerCheck} />;
};

const validateQuestionOrDie = (question: Question) => {
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

const questionTypeToComponentMap = {
  FillInTheBlanks: (props: QuestionProps) => (
    <FillInTheBlanks
      question={props.question as FillInTheBlanksQuestion}
      afterAnswerCheck={props.afterAnswerCheck}
    />
  ),
};
