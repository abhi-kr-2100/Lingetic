"use client";

import { useRouter, useParams } from "next/navigation";
import { ReactNode, useRef, useState } from "react";
import { BookOpen, XCircle } from "lucide-react";
import useQuestions, { SuccessState } from "./useQuestions";
import assert from "@/utilities/assert";
import FillInTheBlanks from "@/app/components/questions/FillInTheBlanks/FillInTheBlanks";
import Translation from "@/app/components/questions/Translation/Translation";
import type {
  AttemptStatus,
  FillInTheBlanksQuestionDTO,
  QuestionDTO,
  TranslationQuestionDTO,
} from "@/utilities/api-types";
import type QuestionProps from "@/app/components/questions/QuestionProps";
import NextButton from "./NextButton";
import { LoadingComponent } from "@/app/loading";

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
      <LoadingComponent>
        <p className="text-[#374151] text-lg">
          Loading your language exercises...
        </p>
      </LoadingComponent>
    );
  }

  if (result.isError) {
    return (
      <div className="h-full bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
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
      <div className="h-full bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
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
    <div className="flex items-center justify-center h-full px-[10%] py-[5%]">
      <RenderQuestion
        question={result.currentQuestion}
        afterAnswerCheck={afterAnswerCheck}
        NextButton={
          <NextButton
            ref={nextButtonRef}
            onNext={result.onNext}
            isLastQuestion={result.isLastQuestion}
          />
        }
      />
    </div>
  );
}

interface LearnPageParams {
  language: string;

  // Not used, but required by useParams
  [key: string]: string | undefined;
}

interface RenderQuestionProps {
  question: QuestionDTO;
  afterAnswerCheck?: (attemptStatus?: AttemptStatus) => void;
  NextButton: ReactNode;
}

const RenderQuestion = ({
  question,
  afterAnswerCheck,
  NextButton,
}: RenderQuestionProps) => {
  validateQuestionOrDie(question);

  const Component = questionTypeToComponentMap[question.questionType];
  return (
    <Component
      question={question}
      afterAnswerCheck={afterAnswerCheck}
      NextButton={NextButton}
    />
  );
};

const validateQuestionOrDie = (question: QuestionDTO) => {
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
      question={props.question as FillInTheBlanksQuestionDTO}
      afterAnswerCheck={props.afterAnswerCheck}
      NextButton={props.NextButton}
    />
  ),
  Translation: (props: QuestionProps) => (
    <Translation
      question={props.question as TranslationQuestionDTO}
      afterAnswerCheck={props.afterAnswerCheck}
      NextButton={props.NextButton}
    />
  ),
};
