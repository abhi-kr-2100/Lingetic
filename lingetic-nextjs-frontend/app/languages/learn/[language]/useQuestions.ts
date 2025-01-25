import { useState } from "react";
import { useQuery } from "@tanstack/react-query";

interface Question {
  id: string;
  type: "FillInTheBlanks";
  text: string;
  hint: string;
}

interface UseQuestionsParams {
  onFinish: () => void;
  language: string;
}

interface LoadingState {
  isLoading: true;
  isError: false;
}

interface ErrorState {
  isLoading: false;
  isError: true;
}

interface SuccessState {
  isLoading: false;
  isError: false;
  currentQuestion: Question;
  isLastQuestion: boolean;
  onNext: () => void;
}

type UseQuestionsResult = LoadingState | ErrorState | SuccessState;

export default function useQuestions({
  onFinish,
  language,
}: UseQuestionsParams): UseQuestionsResult {
  const {
    data: questions = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["questions", language],
    queryFn: () => fetchQuestions(language),

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
    };
  }

  if (isError) {
    return {
      isLoading: false,
      isError: true,
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
    currentQuestion: questions[currentQuestionIndex],
    isLastQuestion: currentQuestionIndex === questions.length - 1,
    onNext,
  };
}

const fetchQuestions = async (language: string): Promise<Question[]> => {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/questions?language=${language}`
  );

  if (!res.ok) {
    throw new Error("Failed to fetch questions");
  }

  return res.json();
};
