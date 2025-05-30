import { useState, useEffect } from "react";
import { useMutation } from "@tanstack/react-query";
import { attemptQuestion } from "@/utilities/api";
import type { AttemptResponse, QuestionDTO } from "@/utilities/api-types";
import assert from "@/utilities/assert";
import { useAuth } from "@clerk/nextjs";

interface TextResponse extends AttemptResponse {
  correctAnswer: string;
}

export default function useTextUserAnswer<Response extends TextResponse>(
  question: QuestionDTO
) {
  assert(question.sentenceID.trim().length > 0, "sentenceID is required");

  const [answer, setAnswer] = useState("");
  const { getToken } = useAuth();

  const attemptChallengeMutation = useMutation<Response, Error, string>({
    mutationFn: (userResponse: string) => {
      const attemptRequest = {
        sentenceID: question.sentenceID,
        questionType: question.questionType,
        userResponse,
      };

      return attemptQuestion(attemptRequest, getToken);
    },
  });

  // For a different question, start with a fresh internal state
  useEffect(() => {
    setAnswer("");
    attemptChallengeMutation.reset();
  }, [question, attemptChallengeMutation.reset]);

  const checkAnswer = async () => {
    // Prevents race conditions where the user might click the button multiple
    // times. Do nothing if the mutation is already fired.
    if (attemptChallengeMutation.isPending) {
      return;
    }

    try {
      const response = await attemptChallengeMutation.mutateAsync(answer);
      return response;
    } catch (err) {
      // Do nothing; isError has been set by the mutation, and can be checked
      // to display an error message.
      console.error(`Error while checking answer: ${err}`);
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
