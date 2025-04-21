import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";

export function mockAudio() {
  class AudioMock {
    src: string = "";
    currentTime: number = 0;
    play = jest.fn().mockImplementation(() => {
      return Promise.resolve();
    });
    pause = jest.fn().mockImplementation(() => {});
  }

  Object.defineProperty(global, "Audio", {
    value: AudioMock,
    writable: true,
  });
}

export const renderWithQueryClient = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>{component}</QueryClientProvider>
  );
};

export function escapeRegex(s: string) {
  return s.replace(/[/\-\\^$*+?.()|[\]{}]/g, "\\$&");
}
