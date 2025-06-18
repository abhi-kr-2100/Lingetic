import type { SourceToTargetTranslationQuestionDTO } from "@/utilities/api-types";
import type { ChangeEvent, KeyboardEvent } from "react";

interface QuestionBoxProps {
    question: SourceToTargetTranslationQuestionDTO;
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
    return (
        <div className="flex flex-col gap-4">
            <div className="text-lg">
                {question.sourceText}
            </div>
            <div>
                <input
                    type="text"
                    value={value}
                    autoFocus
                    onChange={onChange}
                    onKeyDown={onKeyDown}
                    className="w-full border-b-2 border-teal-700 focus:outline-none focus:border-teal-800 text-left py-2"
                    placeholder="Type translation here"
                    disabled={disabled}
                    spellCheck
                    autoCorrect="off"
                    autoCapitalize="off"
                    autoComplete="off"
                />
            </div>
        </div>
    );
}
