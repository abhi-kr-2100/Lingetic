"use client";

import { useRef, useEffect } from "react";

import type {
  FillInTheBlanksAttemptResponse,
  FillInTheBlanksQuestion,
} from "@/utilities/api-types";
import Speaker from "./Speaker";
import useUserAnswer from "./useUserAnswer";
import { languageNameToCode } from "@/app/languages/constants";
import assert from "@/utilities/assert";

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

  const languageCode = languageNameToCode[question.language];
  assert(languageCode !== undefined, `Unknown language: ${question.language}`);

  return (
    <>
      <div className="text-skin-base text-xl flex items-start gap-2 mb-4">
        <Speaker question={question} autoplay />
        <div>
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
            className={`p-2 border rounded w-40 text-center text-xl`}
            disabled={isChecked || isChecking}
            lang={languageCode}
            spellCheck
            autoCorrect="off"
            autoCapitalize="off"
            autoComplete="off"
          />
          <span>{textAfter}</span>
        </div>
      </div>
      <p className="text-skin-base mb-4">Hint: {question.hint}</p>
      {!isChecked && (
        <div className="flex justify-end">
          <button
            disabled={isChecking}
            onClick={() => {
              void handleCheckAnswer();
            }}
            className="bg-[#2563eb] text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center"
          >
            {isChecking ? "Checking..." : "Check"}
          </button>
        </div>
      )}
      {isError && <p>An error occurred! Please try again after some time.</p>}
    </>
  );
}
