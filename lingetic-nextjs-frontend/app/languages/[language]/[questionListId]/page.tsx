"use client";

import { RedirectToSignIn, SignedIn, SignedOut } from "@clerk/nextjs";
import LearnPageComponent from "./LearnPageComponent";

export default function LearnPage() {
  return (
    <>
      <SignedIn>
        <LearnPageComponent />
      </SignedIn>
      <SignedOut>
        <RedirectToSignIn />
      </SignedOut>
    </>
  );
}
