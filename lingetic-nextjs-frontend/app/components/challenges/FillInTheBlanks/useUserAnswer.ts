import { useState } from "react";
import { useMutation } from "@tanstack/react-query";

type AttemptResponse = {
  status: "success" | "failure";
  comment?: string;
  answer?: string;
};

const attemptChallenge = async (
  questionId: string,
  response: string
): Promise<AttemptResponse> => {
  const res = await fetch("/api/challenge/attempt", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ questionId, response }),
  });

  if (!res.ok) {
    throw new Error("Unexpected internal error!");
  }

  return res.json();
};

export default function useUserAnswer(questionId: string) {
  const [answer, setAnswer] = useState("");

  const mutation = useMutation<AttemptResponse, Error, string>({
    mutationFn: (response: string) => attemptChallenge(questionId, response),
  });

  const checkAnswer = () => {
    mutation.mutate(answer);
  };

  return {
    answer,
    setAnswer,
    checkAnswer,
    isChecked: mutation.isSuccess,
    isError: mutation.isError,
    result: mutation.data,
  };
} 
