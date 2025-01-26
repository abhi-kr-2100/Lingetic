"use client";

import { useRouter, useParams } from "next/navigation";

import FillInTheBlanks from "@/app/components/challenges/FillInTheBlanks/FillInTheBlanks";
import useQuestions from "./useQuestions";

type LearnPageParams = {
  language: string;
};

export default function LearnPage() {
  const router = useRouter();
  const { language } = useParams<LearnPageParams>();

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
      <FillInTheBlanks question={result.currentQuestion} />
      <button
        // The controls of the rendered question should have type="submit"
        // buttons.
        type="button"
        onClick={result.onNext}
        className="mt-4 bg-skin-button-primary text-skin-inverted px-4 py-2 rounded transition-colors"
      >
        {result.isLastQuestion ? "Finish" : "Next"}
      </button>
    </div>
  );
}
