import Question from "./Question";
import Result from "../components/Result";
import assert from "@/utilities/assert";

import type {
    TranslationAttemptResponse,
    TranslationQuestionDTO,
} from "@/utilities/api-types";
import type { ChangeEvent, KeyboardEvent } from "react";
import { languageIDToName } from "@/app/languages/constants";

interface MainComponentProps {
    isChecked: boolean;
    attemptResponse: TranslationAttemptResponse | undefined;
    question: TranslationQuestionDTO;
    answer: string;
    handleInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
    handleInputKeyDown: (e: KeyboardEvent<HTMLInputElement>) => void;
    isChecking: boolean;
}

export default function MainComponent({
    isChecked,
    attemptResponse,
    question,
    answer,
    handleInputChange,
    handleInputKeyDown,
    isChecking,
}: MainComponentProps) {
    return (
        <div className="flex flex-col gap-4">
            <div className="text-xl font-semibold">
                {!isChecked ? (
                    <Question
                        question={question}
                        value={answer}
                        onChange={handleInputChange}
                        onKeyDown={handleInputKeyDown}
                        disabled={isChecking}
                    />
                ) : attemptResponse !== undefined ? (
                    <Result sentenceID={question.sentenceID} fullSentence={attemptResponse.correctAnswer} attemptResponse={attemptResponse} />
                ) : (
                    <>
                        {assert(
                            false,
                            "attemptResponse is undefined after successful check"
                        )}
                    </>
                )}
            </div>
            <div className="text-sm text-gray-600">
                Translate to {languageIDToName[question.translateToLanguage]}.
            </div>
        </div>
    );
}
