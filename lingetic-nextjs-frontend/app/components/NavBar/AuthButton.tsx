"use client";

import { SignInButton, useAuth, SignOutButton, useClerk } from "@clerk/nextjs";
import { ButtonHTMLAttributes } from "react";

export default function AuthButton() {
  const { isSignedIn, isLoaded } = useAuth();
  const { redirectToSignIn, signOut } = useClerk();

  if (!isLoaded) {
    return null;
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

const StyledAuthButton = (props: ButtonHTMLAttributes<HTMLButtonElement>) => (
  <button
    {...props}
    className="bg-skin-button-primary text-skin-inverted px-4 py-2 rounded-md hover:bg-skin-button-primary-hover"
  />
);
