import { useEffect, useState } from "react";
import type { FillInTheBlanksQuestion } from "@/utilities/api-types";
import { sha1 } from "@/utilities/helpers";

/**
 * Hook to check if a user's answer is correct for a fill-in-the-blanks question
 * by comparing SHA-1 hash digests, similar to the backend implementation.
 *
 * @param question The fill-in-the-blanks question object
 * @param answer The user's answer
 * @returns A boolean indicating whether the answer is correct
 */
export default function useLocalCheckAnswer(
  question: FillInTheBlanksQuestion,
  answer: string
): boolean {
  const [isCorrect, setIsCorrect] = useState(false);

  useEffect(() => {
    if (!answer.trim()) {
      setIsCorrect(false);
      return;
    }

    const checkAnswer = async () => {
      const fullText = question.text.replace(/_+/, answer);
      const calculatedDigest = await sha1(
        `${fullText.trim()}_${question.language}`
      );

      setIsCorrect(calculatedDigest === question.fullTextDigest);
    };

    void checkAnswer();
  }, [question, answer]);

  return isCorrect;
}
