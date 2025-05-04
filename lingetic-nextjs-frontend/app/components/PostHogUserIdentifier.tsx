"use client";

import { useUser } from "@clerk/nextjs";
import posthog from "posthog-js";
import { useEffect } from "react";

export default function PostHogUserIdentifier() {
  const { user, isSignedIn, isLoaded } = useUser();

  useEffect(() => {
    if (!isLoaded || !isSignedIn) {
      return;
    }

    posthog.identify(user.id, {
      email: user.emailAddresses[0]?.emailAddress,
      fullName: user.fullName,
    });
  }, [isLoaded, isSignedIn, user?.id, user?.emailAddresses, user?.fullName]);

  return <></>;
}
