import type { FillInTheBlanksAttemptResponse } from "@/utilities/api-types";
import WordExplanationHover from "./WordExplanationHover";
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
    <div className="flex flex-wrap gap-1">
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
  );
}
