import React from "react";
import type { FillInTheBlanksAttemptResponse } from "@/utilities/api-types";
import assert from "@/utilities/assert";

interface AnswerCheckStatusProps {
  isError: boolean;
  isChecked: boolean;
  attemptResponse?: FillInTheBlanksAttemptResponse;
}

export default function AnswerCheckStatus({
  isError,
  isChecked,
  attemptResponse,
}: AnswerCheckStatusProps) {
  if (isError) {
    return (
      <>
        <p>An error occurred! Please try again after some time.</p>
        <p className="invisible">Invisible text to reserve space</p>
      </>
    );
  }

  if (!isChecked) {
    return (
      <>
        <p className="invisible">Invisible text to reserve space</p>
        <p className="invisible">Invisible text to reserve space</p>
      </>
    );
  }

  if (attemptResponse === undefined) {
    assert(false, "attemptResponse is undefined after successful check");
    return null;
  }

  if (attemptResponse.attemptStatus === "Success") {
    return (
      <>
        <p className="text-skin-success">Correct!</p>
        <p className="invisible">Invisible text to reserve space</p>
      </>
    );
  }

  if (attemptResponse.attemptStatus === "Failure") {
    return (
      <>
        <p className="text-skin-error">Incorrect.</p>
        {attemptResponse.correctAnswer ? (
          <p>Correct answer: {attemptResponse.correctAnswer}</p>
        ) : (
          <p className="invisible">Invisible text to reserve space</p>
        )}
      </>
    );
  }
}
