import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";

const queryClient = new QueryClient();

export const renderWithQueryClient = (component: React.ReactElement) => {
  return render(
    <QueryClientProvider client={queryClient}>{component}</QueryClientProvider>
  );
};

export function escapeRegex(s: string) {
  return s.replace(/[/\-\\^$*+?.()|[\]{}]/g, "\\$&");
}
