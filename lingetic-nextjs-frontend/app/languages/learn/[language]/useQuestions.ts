import { useState } from "react";

interface UseQuestionsParams {
  questions: Array<{
    id: string;
    type: "FillInTheBlanks";
    text: string;
    hint: string;
  }>;
  onFinish: () => void;
}

export function useQuestions({ questions, onFinish }: UseQuestionsParams) {
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

  const onNext = () => {
    if (currentQuestionIndex < questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    } else {
      onFinish();
    }
  };

  return {
    currentQuestion: questions[currentQuestionIndex],
    isLastQuestion: currentQuestionIndex === questions.length - 1,
    onNext,
  };
} 
