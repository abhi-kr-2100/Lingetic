import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchQuestions } from "@/utilities/api";
import assert from "@/utilities/assert";
import type { Question } from "@/utilities/api-types";
import { useAuth } from "@clerk/nextjs";

interface UseQuestionsParams {
  onFinish: () => void;
  language: string;
  questionListId: string;
}

interface LoadingState {
  isLoading: true;
  isError: false;
  hasQuestions: false;
}

interface ErrorState {
  isLoading: false;
  isError: true;
  hasQuestions: false;
}

interface NoQuestionsState {
  isLoading: false;
  isError: false;
  hasQuestions: false;
}

export interface SuccessState {
  isLoading: false;
  isError: false;
  hasQuestions: true;
  currentQuestion: Question;
  isLastQuestion: boolean;
  onNext: () => void;
  totalQuestions: number;
}

type UseQuestionsResult =
  | LoadingState
  | ErrorState
  | NoQuestionsState
  | SuccessState;

export default function useQuestions({
  onFinish,
  language,
  questionListId,
}: UseQuestionsParams): UseQuestionsResult {
  assert(language.trim().length > 0, "language is required");
  assert(questionListId.trim().length > 0, "questionListId is required");

  const { getToken } = useAuth();

  const {
    data: questions = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["questions", language, questionListId],
    queryFn: () => fetchQuestions(language, questionListId, getToken),

    // The same questions should not be displayed on different renders
    // of the component. This is because the questions that Lingetic thinks
    // the user should attempt may change any time.
    refetchOnMount: "always",

    // If user navigates away from the page in between a run of a playlist,
    // the playlist shouldn't refresh and change the questions the user was
    // working with.
    refetchOnWindowFocus: false,
  });

  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

  if (isLoading) {
    return {
      isLoading: true,
      isError: false,
      hasQuestions: false,
    };
  }

  if (isError) {
    return {
      isLoading: false,
      isError: true,
      hasQuestions: false,
    };
  }

  if (questions.length === 0) {
    return {
      isLoading: false,
      isError: false,
      hasQuestions: false,
    };
  }

  const onNext = () => {
    if (currentQuestionIndex < questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    } else {
      onFinish();
    }
  };

  return {
    isLoading: false,
    isError: false,
    hasQuestions: true,
    currentQuestion: questions.at(currentQuestionIndex) as Question,
    isLastQuestion: currentQuestionIndex === questions.length - 1,
    onNext,
    totalQuestions: questions.length,
  };
}
