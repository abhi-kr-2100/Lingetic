import { useEffect, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  fetchQuestionAsset,
  fetchQuestions,
  getQuestionAssetTypes,
} from "@/utilities/api";
import assert from "@/utilities/assert";
import type { Question } from "@/utilities/api-types";
import { useAuth } from "@clerk/nextjs";

interface UseQuestionsParams {
  onFinish: () => void;
  language: string;
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
}: UseQuestionsParams): UseQuestionsResult {
  assert(language.trim().length > 0, "language is required");

  const { getToken } = useAuth();

  const {
    data: questions = [],
    isError,
    isFetching,
    isSuccess,
  } = useQuery({
    queryKey: ["questions", language],
    queryFn: () => fetchQuestions(language, getToken),

    // Don't refetch as questions have been prefetched on the results page
    refetchOnMount: false,
    refetchOnReconnect: false,

    // If user navigates away from the page in between a run of a playlist,
    // the playlist shouldn't refresh and change the questions the user was
    // working with.
    refetchOnWindowFocus: false,
  });

  const queryClient = useQueryClient();

  useEffect(() => {
    if (!isSuccess) {
      return;
    }

    // prefetch question assets
    questions.forEach((question) => {
      const assetTypes = getQuestionAssetTypes(question.questionType);
      assetTypes.forEach((assetType) => {
        queryClient.prefetchQuery({
          queryKey: ["questionAssets", question.id, assetType],
          queryFn: () => fetchQuestionAsset(question.id, assetType),
          // 1 hour; assumed time users would spend with a single question
          staleTime: 1 * 60 * 60 * 1000,
        });
      });
    });
  }, [isSuccess, questions, queryClient]);

  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

  if (isFetching) {
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
