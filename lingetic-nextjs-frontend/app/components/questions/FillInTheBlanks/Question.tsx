"use client";

import { useRef, useEffect } from "react";

import type {
  FillInTheBlanksAttemptResponse,
  FillInTheBlanksQuestion,
} from "@/utilities/api-types";
import Speaker from "./Speaker";
import useUserAnswer from "./useUserAnswer";

interface QuestionBoxProps {
  question: FillInTheBlanksQuestion;
  onUserAnswerCheck: (attemptResponse?: FillInTheBlanksAttemptResponse) => void;
}

export default function QuestionBox({
  question,
  onUserAnswerCheck,
}: QuestionBoxProps) {
  const { answer, setAnswer, checkAnswer, isChecking, isChecked, isError } =
    useUserAnswer(question.id);

  const handleCheckAnswer = async () => {
    const response = await checkAnswer();
    onUserAnswerCheck(response);
  };

  const [textBefore, textAfter] = question.text.split(/_+/);

  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    // Focus the input field on first render, and also when the question
    // changes.

    if (!inputRef.current) {
      return;
    }

    // The input field could be disabled due to a previous question. A disabled
    // input field cannot be focused.
    inputRef.current.disabled = false;
    inputRef.current.focus();
  }, [question.id]);

  return (
    <>
      <div className="text-skin-base text-xl mb-4 flex items-center gap-2">
        <Speaker question={question} autoplay />
        <span>{textBefore}</span>
        <input
          type="text"
          value={answer}
          ref={inputRef}
          onChange={(e) => {
            setAnswer(e.target.value);
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !isChecked) {
              void handleCheckAnswer();
            }
          }}
          className={`p-2 border rounded w-40 text-center`}
          disabled={isChecked || isChecking}
        />
        <span>{textAfter}</span>
      </div>
      <p className="text-skin-base mb-4">Hint: {question.hint}</p>
      {!isChecked && (
        <button
          disabled={isChecking}
          onClick={() => {
            void handleCheckAnswer();
          }}
          className={`bg-skin-button-primary text-skin-inverted px-4 py-2 rounded transition-colors ${
            isChecking ? "opacity-70" : ""
          }`}
        >
          {isChecking ? "Checking..." : "Check"}
        </button>
      )}
      {isError && <p>An error occurred! Please try again after some time.</p>}
    </>
  );
}
