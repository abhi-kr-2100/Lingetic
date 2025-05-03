"use client";

import { useEffect, useState } from "react";

import assert from "@/utilities/assert";
import type QuestionProps from "../QuestionProps";
import type {
  FillInTheBlanksQuestion,
  FillInTheBlanksAttemptResponse,
} from "@/utilities/api-types";

import Question from "./Question";
import Result from "./Result";

interface FillInTheBlanksProps extends QuestionProps {
  question: FillInTheBlanksQuestion;
}

export default function FillInTheBlanks({
  question,
  afterAnswerCheck,
  NextButton,
}: FillInTheBlanksProps) {
  validateQuestionOrDie(question);

  const [attemptResponse, setAttemptResponse] = useState<
    FillInTheBlanksAttemptResponse | undefined
  >(undefined);

  useEffect(() => {
    // Reset the attempt response when the question changes.
    setAttemptResponse(undefined);
  }, [question.id]);

  function onUserAnswerCheck(response?: FillInTheBlanksAttemptResponse) {
    setAttemptResponse(response);
    afterAnswerCheck?.(response?.attemptStatus);
  }

  return (
    <div className="shadow-lg rounded-lg p-6">
      <div className="mb-4">
        {attemptResponse === undefined ? (
          <Question question={question} onUserAnswerCheck={onUserAnswerCheck} />
        ) : (
          <>
            <Result question={question} attemptResponse={attemptResponse} />
            <div className="flex justify-end mt-4">{NextButton}</div>
          </>
        )}
      </div>
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
