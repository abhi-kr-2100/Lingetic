import type {
  SourceToTargetTranslationAttemptResponse,
  SourceToTargetTranslationQuestion,
} from "@/utilities/api-types";

interface ResultBoxProps {
  question: SourceToTargetTranslationQuestion;
  attemptResponse: SourceToTargetTranslationAttemptResponse;
}

export default function Result({ question, attemptResponse }: ResultBoxProps) {
  return (
    <div className="flex flex-col gap-4">
      <p className="text-xl font-semibold">{attemptResponse.correctAnswer}</p>
      <p className="text-skin-base italic">{question.translation}</p>
    </div>
  );
}
