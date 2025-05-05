"use client";

import { useSearchParams, useParams } from "next/navigation";
import Link from "next/link";
import { RedirectToSignIn, SignedIn, SignedOut } from "@clerk/nextjs";
import { Trophy } from "lucide-react";
import { useRef, useEffect } from "react";
import assert from "@/utilities/assert";

export default function ResultsPage() {
  return (
    <>
      <SignedIn>
        <ResultsPageComponent />
      </SignedIn>
      <SignedOut>
        <RedirectToSignIn />
      </SignedOut>
    </>
  );
}

interface ResultsPageParams {
  language: string;
  [key: string]: string;
}

function ResultsPageComponent() {
  const playAgainRef = useRef<HTMLAnchorElement>(null);
  useEffect(() => {
    playAgainRef.current?.focus();
  }, []);

  const searchParams = useSearchParams();
  const { language } = useParams<ResultsPageParams>();

  const totalStr = searchParams.get("total") ?? "NaN";
  const correctStr = searchParams.get("correct") ?? "NaN";

  const total = parseInt(totalStr);
  const correct = parseInt(correctStr);

  assert(
    !Number.isNaN(total) && !Number.isNaN(correct),
    "total or correct is NaN"
  );

  assert(total > 0, "total must be greater than 0");
  assert(correct >= 0, "correct must be non-negative");

  const percentage = Math.round((correct / total) * 100);

  return (
    <div className="flex-1 bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
      <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
        <Trophy className="h-16 w-16 text-[#2563eb] mx-auto mb-6" />
        <h1 className="text-3xl font-bold text-[#374151] mb-4">
          Practice Complete!
        </h1>
        <p className="text-lg text-[#374151] mb-8">
          You got <span className="font-bold text-[#2563eb]">{correct}</span>{" "}
          out of <span className="font-bold">{total}</span> questions correct (
          {percentage}%)
        </p>
        <div className="flex flex-col gap-4">
          <Link
            ref={playAgainRef}
            href={`/languages/${language}`}
            className="w-full bg-[#2563eb] text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors inline-block text-center focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Play Again
          </Link>
        </div>
      </div>
    </div>
  );
}
