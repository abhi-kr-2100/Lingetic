"use client";

import { PropsWithChildren } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ClerkProvider } from "@clerk/nextjs";
import useReactQueryDevtools from "./hooks/useReactQueryDevtools";

const queryClient = new QueryClient();

export default function Providers({ children }: PropsWithChildren) {
  const ReactQueryDevtools = useReactQueryDevtools();

  return (
    <ClerkProvider>
      <QueryClientProvider client={queryClient}>
        {children}
        {ReactQueryDevtools}
      </QueryClientProvider>
    </ClerkProvider>
  );
}
