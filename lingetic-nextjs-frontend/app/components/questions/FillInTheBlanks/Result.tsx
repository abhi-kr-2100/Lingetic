"use client";

import type { FillInTheBlanksAttemptResponse } from "@/utilities/api-types";
import WordExplanationHover from "./WordExplanationHover";
import Speaker from "./Speaker";
import type { FillInTheBlanksQuestion } from "@/utilities/api-types";
import assert from "@/utilities/assert";

interface ResultBoxProps {
  question: FillInTheBlanksQuestion;
  attemptResponse: FillInTheBlanksAttemptResponse;
}

export default function ResultBox({
  question,
  attemptResponse,
}: ResultBoxProps) {
  assert(
    attemptResponse.explanation.length > 0,
    `explanation not available for question ${question.id}`
  );

  return (
    <>
      <div className="text-skin-base text-xl flex items-center gap-2 flex-wrap mb-4">
        <Speaker question={question} />
        {attemptResponse.explanation
          .slice()
          .sort((a, b) => a.sequenceNumber - b.sequenceNumber)
          .map((exp) => (
            <WordExplanationHover
              key={exp.sequenceNumber}
              word={exp.word}
              explanation={exp}
            />
          ))}
      </div>
      <p
        className={`mb-4 ${
          attemptResponse.attemptStatus === "Success"
            ? "text-skin-success"
            : "text-skin-error"
        }`}
      >
        {attemptResponse.attemptStatus === "Success"
          ? "Correct!"
          : "Incorrect."}
      </p>
      {attemptResponse.attemptStatus === "Failure" &&
        attemptResponse.correctAnswer && (
          <p>Correct answer: {attemptResponse.correctAnswer}</p>
        )}
    </>
  );
}
