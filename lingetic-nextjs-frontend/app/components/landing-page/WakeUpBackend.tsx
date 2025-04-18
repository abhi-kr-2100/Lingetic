"use client";

import { wakeUpBackend } from "@/utilities/api";
import { useEffect } from "react";

export default function WakeUpBackend() {
  useEffect(() => {
    wakeUpBackend();
  }, []);

  return <></>;
}
