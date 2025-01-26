import { screen, fireEvent, waitFor } from "@testing-library/react";
import LearnPage from "@/app/languages/learn/[language]/page";
import { renderWithQueryClient } from "@/utilities/testing-helpers";

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
  useParams: () => ({ language: "spanish" }),
}));

global.fetch = jest.fn();

const mockSuccessfulFetch = () => {
  (global.fetch as jest.Mock).mockImplementation(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve(mockQuestions),
    })
  );
};

const mockQuestions = [
  {
    id: "1",
    type: "FillInTheBlanks" as const,
    text: "The cat ____ lazily on the windowsill.",
    hint: "straighten or extend one's body",
  },
  {
    id: "2",
    type: "FillInTheBlanks" as const,
    text: "She ____ her coffee every morning.",
    hint: "to drink",
  },
  {
    id: "3",
    type: "FillInTheBlanks" as const,
    text: "The children ____ in the park yesterday.",
    hint: "to have fun or recreation",
  },
  {
    id: "4",
    type: "FillInTheBlanks" as const,
    text: "He ____ the piano beautifully.",
    hint: "to create music with an instrument",
  },
  {
    id: "5",
    type: "FillInTheBlanks" as const,
    text: "They ____ dinner at 7 PM.",
    hint: "to consume food",
  },
];

describe("LearnPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("shows loading text when loading", async () => {
    // Mock a pending promise that never resolves to simulate loading state
    (global.fetch as jest.Mock).mockImplementation(() => new Promise(() => {}));

    renderWithQueryClient(<LearnPage />);
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it("shows error text when there is an error", async () => {
    // Mock a failed fetch
    (global.fetch as jest.Mock).mockImplementation(() =>
      Promise.reject("API Error")
    );

    renderWithQueryClient(<LearnPage />);

    await waitFor(() => {
      expect(screen.getByText(/failed/i)).toBeInTheDocument();
    });
  });

  it("shows error when request resolves unsuccessfully", async () => {
    (global.fetch as jest.Mock).mockImplementation(() =>
      Promise.resolve({
        ok: false,
        status: 400,
        statusText: "Bad Request",
      })
    );

    renderWithQueryClient(<LearnPage />);

    await waitFor(() => {
      expect(screen.getByText(/failed/i)).toBeInTheDocument();
    });
  });

  it("shows the current question when loaded successfully", async () => {
    // Mock a successful fetch
    mockSuccessfulFetch();

    renderWithQueryClient(<LearnPage />);

    await waitFor(() => {
      expect(screen.getByText(/the cat/i)).toBeInTheDocument();
    });
  });

  it("shows a next button when not on the last question", async () => {
    mockSuccessfulFetch();

    renderWithQueryClient(<LearnPage />);

    await waitFor(() => {
      expect(screen.getByText(/next/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/next/i));
    expect(screen.getByText(/her coffee/i)).toBeInTheDocument();
  });

  it("advances to the next question when Next button is clicked", async () => {
    mockSuccessfulFetch();

    renderWithQueryClient(<LearnPage />);

    await waitFor(() => {
      expect(screen.getByText(/the cat/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/next/i));

    expect(screen.getByText(/her coffee/i)).toBeInTheDocument();
  });

  it("shows a finish button on the last question", async () => {
    mockSuccessfulFetch();

    renderWithQueryClient(<LearnPage />);

    // Click through all questions except the last one
    for (let i = 0; i < mockQuestions.length - 1; i++) {
      await waitFor(() => {
        expect(screen.getByText(/next/i)).toBeInTheDocument();
      });
      fireEvent.click(screen.getByText(/next/i));
    }

    await waitFor(() => {
      expect(screen.getByText(/finish/i)).toBeInTheDocument();
    });
  });

  it("redirects when finish button is clicked", async () => {
    mockSuccessfulFetch();

    renderWithQueryClient(<LearnPage />);

    // Click through all questions to get to the last one
    for (let i = 0; i < mockQuestions.length - 1; i++) {
      await waitFor(() => {
        expect(screen.getByText(/next/i)).toBeInTheDocument();
      });
      fireEvent.click(screen.getByText(/next/i));
    }

    await waitFor(() => {
      expect(screen.getByText(/finish/i)).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText(/finish/i));

    expect(mockPush).toHaveBeenCalled();
  });
});
