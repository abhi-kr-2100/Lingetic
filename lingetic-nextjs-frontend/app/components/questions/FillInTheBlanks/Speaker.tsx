"use client";

import { Volume2 } from "lucide-react";

import useQuestionAudioPlayback from "./useQuestionAudioPlayback";
import type { FillInTheBlanksQuestionDTO } from "@/utilities/api-types";

interface SpeakerProps {
  question: FillInTheBlanksQuestionDTO;
  autoplay?: boolean;
}

export default function Speaker({ question, autoplay = false }: SpeakerProps) {
  const { playAudio, isLoading, isError } = useQuestionAudioPlayback({
    question,
    autoplay,
  });

  if (isLoading || isError) {
    return null;
  }

  return (
    <button
      onClick={playAudio}
      type="button"
      aria-label="Play audio"
      className="border border-skin-base text-skin-base bg-transparent px-6 py-3 rounded-lg font-medium flex items-center"
    >
      <Volume2 size={24} strokeWidth={2} />
    </button>
  );
}
