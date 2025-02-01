import { useState, useEffect } from "react";
import { useMutation } from "@tanstack/react-query";
import { attemptQuestion } from "@/utilities/api";
import type {
  FillInTheBlanksAttemptRequest,
  FillInTheBlanksAttemptResponse,
} from "@/utilities/api-types";
import assert from "@/utilities/assert";

export default function useUserAnswer(questionID: string) {
  assert(questionID?.trim()?.length > 0, "questionId is required");

  const [answer, setAnswer] = useState("");

  const attemptChallengeMutation = useMutation<
    FillInTheBlanksAttemptResponse,
    Error,
    string
  >({
    mutationFn: (userResponse: string) => {
      const attemptRequest = {
        questionID,
        questionType: "FillInTheBlanks",
        userResponse,
      } as FillInTheBlanksAttemptRequest;

      return attemptQuestion(attemptRequest);
    },
  });

  // For a different question, start with a fresh internal state
  useEffect(() => {
    setAnswer("");
    attemptChallengeMutation.reset();
  }, [questionID]);

  const checkAnswer = async () => {
    if (attemptChallengeMutation.isPending) {
      return;
    }

    try {
      await attemptChallengeMutation.mutateAsync(answer);
    } catch (error) {
      // Do nothing; isError has been set by the mutation, and can be checked
      // to display an error message.
    }
  };

  return {
    answer,
    setAnswer,
    checkAnswer,
    isChecking: attemptChallengeMutation.isPending,
    isChecked: attemptChallengeMutation.isSuccess,
    isError: attemptChallengeMutation.isError,
    result: attemptChallengeMutation.data,
  };
}
