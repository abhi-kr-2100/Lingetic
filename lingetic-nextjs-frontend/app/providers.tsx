"use client";

import { PropsWithChildren } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ClerkProvider } from "@clerk/nextjs";
import useReactQueryDevtools from "./hooks/useReactQueryDevtools";
import { PostHogProvider } from "./components/PostHogProvider";

const queryClient = new QueryClient();

export default function Providers({ children }: PropsWithChildren) {
  const ReactQueryDevtools = useReactQueryDevtools();

  return (
    <PostHogProvider>
      <ClerkProvider>
        <QueryClientProvider client={queryClient}>
          {children}
          {ReactQueryDevtools}
        </QueryClientProvider>
      </ClerkProvider>
    </PostHogProvider>
  );
}
