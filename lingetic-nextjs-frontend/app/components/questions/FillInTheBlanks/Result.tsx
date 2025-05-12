import type {
  FillInTheBlanksAttemptResponse,
  WordExplanation,
} from "@/utilities/api-types";
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
  const fullSentence = question.text.replace(
    /_+/,
    attemptResponse.correctAnswer
  );

  const parts = getSentenceParts(fullSentence, attemptResponse.explanation);

  return (
    <div className="flex flex-wrap items-center whitespace-pre-wrap">
      {parts.map((part, idx) =>
        part.explanation ? (
          <WordExplanationHover
            key={idx}
            word={part.text}
            explanation={part.explanation}
          />
        ) : (
          <span key={idx}>{part.text}</span>
        )
      )}
    </div>
  );
}

interface SentencePart {
  text: string;
  explanation?: WordExplanation;
}

function getSentenceParts(
  fullSentence: string,
  explanations: WordExplanation[]
): SentencePart[] {
  const sortedExplanations = explanations.toSorted(
    (a, b) => a.startIndex - b.startIndex
  );

  const parts: SentencePart[] = [];

  let lastIndex = 0;
  sortedExplanations.forEach((explanation) => {
    if (explanation.startIndex > lastIndex) {
      parts.push({
        text: fullSentence.slice(lastIndex, explanation.startIndex),
      });
    }

    parts.push({
      text: explanation.word,
      explanation,
    });

    lastIndex = explanation.startIndex + explanation.word.length;
  });

  if (lastIndex < fullSentence.length) {
    parts.push({
      text: fullSentence.slice(lastIndex),
    });
  }

  return parts;
}
