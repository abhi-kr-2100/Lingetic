"use client";

import { useEffect, useRef } from "react";
import assert from "@/utilities/assert";
import useUserAnswer from "./useUserAnswer";
import type QuestionProps from "../QuestionProps";
import type { FillInTheBlanksQuestion } from "@/utilities/api-types";

interface FillInTheBlanksProps extends QuestionProps {
  question: FillInTheBlanksQuestion;
}

export default function FillInTheBlanks({
  question,
  afterAnswerCheck,
}: FillInTheBlanksProps) {
  validateQuestionOrDie(question);

  const {
    answer,
    setAnswer,
    checkAnswer,
    isChecking,
    isChecked,
    isError,
    result,
  } = useUserAnswer(question.id);

  const handleCheckAnswer = async () => {
    const response = await checkAnswer();
    afterAnswerCheck?.(response?.attemptStatus);
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
    <div className="shadow-lg rounded-lg p-6">
      <div className="text-skin-base text-xl mb-4 flex items-center gap-2">
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
          className="p-2 border rounded w-40 text-center"
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
      {isChecked && result && (
        <div>
          <p
            className={`mb-4 ${
              result.attemptStatus === "Success"
                ? "text-skin-success"
                : "text-skin-error"
            }`}
          >
            {result.attemptStatus === "Success" ? "Correct!" : "Incorrect."}
          </p>
          {result.attemptStatus === "Failure" && result.correctAnswer && (
            <p>Correct answer: {result.correctAnswer}</p>
          )}
        </div>
      )}
      {isError && <p>An error occurred! Please try again after some time.</p>}
    </div>
  );
}

function validateQuestionOrDie(question: FillInTheBlanksQuestion) {
  assert(question != null, "question is null or undefined");
  assert(question.id?.trim()?.length > 0, "question.id is empty");
  assert(
    question.questionType === "FillInTheBlanks",
    "question.questionType is not FillInTheBlanks"
  );
  assert(question.text?.trim()?.length > 0, "question.text is empty");
  assert(
    question.text.includes("_"),
    "question.text does not contain any blank"
  );
  assert(
    question.text?.match(/_+/g)?.length === 1,
    "question.text contains more than one blank"
  );
}
