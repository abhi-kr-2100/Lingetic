import { languageNameToCode } from "@/app/languages/constants";
import assert from "@/utilities/assert";

import type { FillInTheBlanksQuestion } from "@/utilities/api-types";
import type { ChangeEvent, KeyboardEvent } from "react";

interface QuestionBoxProps {
  question: FillInTheBlanksQuestion;
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

  const [textBefore, textAfter] = question.text.split(/_+/);

  return (
    <div>
      <span>{textBefore}</span>
      <input
        type="text"
        value={value}
        autoFocus
        onChange={onChange}
        onKeyDown={onKeyDown}
        className={`border-b-2 border-teal-700 focus:outline-none focus:border-teal-800 text-center max-w-[15ch]`}
        placeholder="type here"
        disabled={disabled}
        lang={languageCode}
        spellCheck
        autoCorrect="off"
        autoCapitalize="off"
        autoComplete="off"
      />
      <span>{textAfter}</span>
    </div>
  );
}
