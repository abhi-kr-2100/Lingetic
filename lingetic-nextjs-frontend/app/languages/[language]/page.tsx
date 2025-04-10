"use client";

import { RedirectToSignIn, SignedIn, SignedOut } from "@clerk/nextjs";
import QuestionListComponent from "./QuestionListComponent";

export default function LanguagePage() {
  return (
    <>
      <SignedIn>
        <QuestionListComponent />
      </SignedIn>
      <SignedOut>
        <RedirectToSignIn />
      </SignedOut>
    </>
  );
}
