import { FillInTheBlanksQuestion } from "@/utilities/api-types";
import log from "@/utilities/logger";
import { useRef, useCallback, useEffect } from "react";

interface UseQuestionAudioPlaybackParams {
  question: FillInTheBlanksQuestion;
  autoplay?: boolean;
}

export default function useQuestionAudioPlayback({
  question,
  autoplay,
}: UseQuestionAudioPlaybackParams) {
  const audioRef = useRef<HTMLAudioElement | null>(null);

  const cleanUpAudio = useCallback(() => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
      audioRef.current.src = "";
      audioRef.current = null;
    }
  }, []);

  const playAudio = useCallback(() => {
    cleanUpAudio();

    const audio = new Audio(
      `${process.env.NEXT_PUBLIC_SPEECH_RECORDINGS_URL_PREFIX}/${question.id}.mp3`
    );
    audioRef.current = audio;
    audio.play().catch((e) => {
      if (e.name === "AbortError") {
        return;
      }

      log(`Error playing audio for question ID ${question.id}: ${e}`, "error");
    });
  }, [question.id, cleanUpAudio]);

  useEffect(() => {
    if (autoplay) {
      playAudio();
    }

    return cleanUpAudio;
  }, [playAudio, autoplay, cleanUpAudio]);

  return { playAudio };
}
