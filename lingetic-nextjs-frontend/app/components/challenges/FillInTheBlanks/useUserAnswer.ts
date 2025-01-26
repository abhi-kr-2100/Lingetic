import { useState, useEffect } from "react";
import { useMutation } from "@tanstack/react-query";
import { attemptQuestion, AttemptResponse } from "@/utilities/api";

export default function useUserAnswer(questionId: string) {
  const [answer, setAnswer] = useState("");

  const attempChallengeMutation = useMutation<AttemptResponse, Error, string>({
    mutationFn: (response: string) => attemptQuestion(questionId, response),
  });

  // For a different question, start with a fresh internal state
  useEffect(() => {
    setAnswer("");
    attempChallengeMutation.reset();
  }, [questionId]);

  const checkAnswer = async () => {
    if (attempChallengeMutation.isPending) {
      return;
    }

    try {
      await attempChallengeMutation.mutateAsync(answer);
    } catch (error) {
      // Do nothing; isError has been set by the mutation, and can be checked
      // to display an error message.
    }
  };

  return {
    answer,
    setAnswer,
    checkAnswer,
    isChecked: attempChallengeMutation.isSuccess,
    isError: attempChallengeMutation.isError,
    result: attempChallengeMutation.data,
  };
}
