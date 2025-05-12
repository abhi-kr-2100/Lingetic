import type { FillInTheBlanksAttemptResponse } from "@/utilities/api-types";
import WordExplanationHover from "./WordExplanationHover";
import type { FillInTheBlanksQuestion } from "@/utilities/api-types";

interface ResultBoxProps {
  question: FillInTheBlanksQuestion;
  attemptResponse: FillInTheBlanksAttemptResponse;
}

export default function ResultBox({
  question,
  attemptResponse,
}: ResultBoxProps) {
  if (attemptResponse.explanation.length === 0) {
    const fullSentence = question.text.replace(
      /_+/,
      attemptResponse.correctAnswer
    );
    return <div>{fullSentence}</div>;
  }

  return (
    <div className="flex flex-wrap gap-1">
      {attemptResponse.explanation
        .slice()
        .sort((a, b) => a.startIndex - b.startIndex)
        .map((exp) => (
          <WordExplanationHover
            key={exp.startIndex}
            word={exp.word}
            explanation={exp}
          />
        ))}
    </div>
  );
}
