"use client";

import { Volume2 } from "lucide-react";

import useQuestionAudioPlayback from "./useQuestionAudioPlayback";
import type { FillInTheBlanksQuestion } from "@/utilities/api-types";

interface SpeakerProps {
  question: FillInTheBlanksQuestion;
  autoplay?: boolean;
}

export default function Speaker({ question, autoplay = false }: SpeakerProps) {
  const { playAudio } = useQuestionAudioPlayback({ question, autoplay });

  return (
    <button onClick={playAudio} type="button" aria-label="Play audio">
      <Volume2 size={46} strokeWidth={1} />
    </button>
  );
}
