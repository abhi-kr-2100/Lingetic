"use client";

import { SignInButton, useAuth, SignOutButton, useClerk } from "@clerk/nextjs";
import LoadingButton from "./LoadingButton";
import StyledAuthButton from "./StyledAuthButton";

export default function AuthButton() {
  const { isSignedIn, isLoaded } = useAuth();
  const { redirectToSignIn, signOut } = useClerk();

  if (!isLoaded) {
    return <LoadingButton />;
  }

  return isSignedIn ? (
    <SignOutButton>
      <StyledAuthButton onClick={() => signOut()}>Sign out</StyledAuthButton>
    </SignOutButton>
  ) : (
    <SignInButton>
      <StyledAuthButton onClick={() => redirectToSignIn()}>
        Sign in
      </StyledAuthButton>
    </SignInButton>
  );
}
