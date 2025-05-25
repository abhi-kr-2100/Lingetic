import { FillInTheBlanksQuestionDTO } from "@/utilities/api-types";
import log from "@/utilities/logger";
import { useRef, useCallback, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchQuestionAsset } from "@/utilities/api";

interface UseQuestionAudioPlaybackParams {
  question: FillInTheBlanksQuestionDTO;
  autoplay?: boolean;
}

export default function useQuestionAudioPlayback({
  question,
  autoplay,
}: UseQuestionAudioPlaybackParams) {
  const audioRef = useRef<HTMLAudioElement | null>(null);

  const {
    isLoading,
    isError,
    data: audioBlob,
  } = useQuery({
    queryKey: ["questionAssets", question.sentenceID, "audio"],
    queryFn: () => fetchQuestionAsset(question.sentenceID, "audio"),
    refetchOnMount: false,
    // Disable automatic refetching since static data can never change
    enabled: false,
  });

  const cleanUpAudio = useCallback(() => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
      audioRef.current.src = "";
      audioRef.current = null;
    }
  }, []);

  const playAudio = useCallback(() => {
    if (!audioBlob) {
      log(
        `No audio blob available for sentence ID ${question.sentenceID}`,
        "error"
      );
      return;
    }

    cleanUpAudio();

    const audio = new Audio();
    audioRef.current = audio;

    const objectUrl = URL.createObjectURL(audioBlob);
    audio.src = objectUrl;

    audio
      .play()
      .catch((e) => {
        if (e.name === "AbortError") {
          return;
        }
        log(
          `Error playing audio for sentence ID ${question.sentenceID}: ${e}`,
          "error"
        );
      })
      .finally(() => {
        URL.revokeObjectURL(objectUrl);
      });
  }, [audioBlob, question.sentenceID, cleanUpAudio]);

  useEffect(() => {
    if (autoplay && audioBlob) {
      playAudio();
    }

    return cleanUpAudio;
  }, [playAudio, autoplay, audioBlob, cleanUpAudio]);

  if (isLoading) {
    return {
      isLoading: true,
      isError: false,
      playAudio: () => {},
    };
  }

  return { playAudio, isLoading, isError };
}
