import React from "react";
import type { FillInTheBlanksAttemptResponse } from "@/utilities/api-types";
import assert from "@/utilities/assert";
import AnswerFeedbackBox from "./AnswerFeedbackBox";
import DiffHighlight from "./DiffHighlight";

interface AnswerCheckStatusProps {
  isError: boolean;
  isChecked: boolean;
  attemptResponse?: FillInTheBlanksAttemptResponse;
  userAnswer: string;
}

export default function AnswerCheckStatus({
  isError,
  isChecked,
  attemptResponse,
  userAnswer,
}: AnswerCheckStatusProps) {
  if (isError) {
    return (
      <AnswerFeedbackBox>
        <p className="text-skin-error">
          An error occurred! Please try again after some time.
        </p>
      </AnswerFeedbackBox>
    );
  }

  if (!isChecked) {
    return null;
  }

  if (attemptResponse === undefined) {
    assert(false, "attemptResponse is undefined after successful check");
    return null;
  }

  if (attemptResponse.attemptStatus === "Success") {
    return (
      <AnswerFeedbackBox>
        <p className="text-skin-success">Correct!</p>
      </AnswerFeedbackBox>
    );
  }

  if (attemptResponse.attemptStatus === "Failure") {
    return (
      <AnswerFeedbackBox className="flex flex-col gap-2">
        <p className="text-skin-error">Incorrect.</p>
        <p>
          Correct Answer:{" "}
          <span className="text-green-800">
            {attemptResponse.correctAnswer}
          </span>
        </p>
        <p>
          You entered: <span className="text-red-800">{userAnswer}</span>
        </p>
        <p>
          Difference:{" "}
          <DiffHighlight
            userAnswer={userAnswer}
            correctAnswer={attemptResponse.correctAnswer}
          />
        </p>
      </AnswerFeedbackBox>
    );
  }
}
