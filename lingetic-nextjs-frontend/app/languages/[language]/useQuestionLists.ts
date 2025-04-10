import { useQuery } from "@tanstack/react-query";
import { fetchQuestionLists } from "@/utilities/api";
import assert from "@/utilities/assert";
import type { QuestionList } from "@/utilities/api-types";
import { useAuth } from "@clerk/nextjs";

interface UseQuestionListsParams {
  language: string;
}

interface LoadingState {
  isLoading: true;
  isError: false;
  hasLists: false;
}

interface ErrorState {
  isLoading: false;
  isError: true;
  hasLists: false;
}

interface SuccessState {
  isLoading: false;
  isError: false;
  hasLists: true;
  lists: QuestionList[];
}

type UseQuestionListsResult = LoadingState | ErrorState | SuccessState;

export default function useQuestionLists({
  language,
}: UseQuestionListsParams): UseQuestionListsResult {
  assert(language.trim().length > 0, "language is required");
  const { getToken } = useAuth();

  const {
    data: lists = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["questionLists", language],
    queryFn: () => fetchQuestionLists(language, getToken),
    refetchOnWindowFocus: true,
  });

  if (isLoading) {
    return {
      isLoading: true,
      isError: false,
      hasLists: false,
    };
  }

  if (isError) {
    return {
      isLoading: false,
      isError: true,
      hasLists: false,
    };
  }

  return {
    isLoading: false,
    isError: false,
    hasLists: true,
    lists,
  };
}