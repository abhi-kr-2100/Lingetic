import { languageNameToCode } from "@/app/languages/constants";
import assert from "@/utilities/assert";

import type { SourceToTargetTranslationQuestion } from "@/utilities/api-types";
import type { ChangeEvent, KeyboardEvent } from "react";

interface QuestionBoxProps {
  question: SourceToTargetTranslationQuestion;
  value: string;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  onKeyDown: (e: KeyboardEvent<HTMLInputElement>) => void;
  disabled: boolean;
}

export default function QuestionBox({
  question,
  value,
  onChange,
  onKeyDown,
  disabled,
}: QuestionBoxProps) {
  const languageCode = languageNameToCode[question.language];
  assert(languageCode !== undefined, `Unknown language: ${question.language}`);

  return (
    <div className="flex flex-col gap-4">
      <div>{question.translation}</div>
      <input
        type="text"
        value={value}
        autoFocus
        onChange={onChange}
        onKeyDown={onKeyDown}
        className="border-b-2 border-teal-700 focus:outline-none focus:border-teal-800 text-center w-full"
        placeholder="Type your translation here"
        disabled={disabled}
        lang={languageCode}
        spellCheck
        autoCorrect="off"
        autoCapitalize="off"
        autoComplete="off"
      />
    </div>
  );
}
