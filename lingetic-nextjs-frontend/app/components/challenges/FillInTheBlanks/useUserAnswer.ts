import { useState, useEffect } from "react";
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
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/challenge/attempt`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ questionId, response }),
    }
  );

  if (!res.ok) {
    throw new Error("Unexpected internal error!");
  }

  return res.json();
};

export default function useUserAnswer(questionId: string) {
  const [answer, setAnswer] = useState("");

  const attempChallengeMutation = useMutation<AttemptResponse, Error, string>({
    mutationFn: (response: string) => attemptChallenge(questionId, response),
  });

  // For a different question, start with a fresh internal state
  useEffect(() => {
    setAnswer("");
    attempChallengeMutation.reset();
  }, [questionId]);

  const checkAnswer = () => {
    attempChallengeMutation.mutate(answer);
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
