"use client";

import { useRouter, useParams } from "next/navigation";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

import FillInTheBlanks from "@/app/components/challenges/FillInTheBlanks/FillInTheBlanks";
import questions from "./mockQuestions";

import { useQuestions } from "./useQuestions";

const queryClient = new QueryClient();

type LearnPageParams = {
  language: string;
};

export default function LearnPage() {
  const router = useRouter();
  const { language } = useParams<LearnPageParams>();

  const { currentQuestion, isLastQuestion, onNext } = useQuestions({
    questions,
    onFinish: () => router.push("/languages"),
  });

  return (
    <QueryClientProvider client={queryClient}>
      <div className="container mx-auto p-4">
        <h1 className="text-skin-base text-3xl font-bold mb-6">
          Learning {language}
        </h1>
        <FillInTheBlanks question={currentQuestion} />
        <button
          onClick={onNext}
          className="mt-4 bg-skin-button-primary text-skin-inverted px-4 py-2 rounded transition-colors"
        >
          {isLastQuestion ? "Finish" : "Next"}
        </button>
      </div>
    </QueryClientProvider>
  );
}
