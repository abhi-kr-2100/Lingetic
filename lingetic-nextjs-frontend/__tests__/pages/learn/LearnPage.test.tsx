import { fireEvent, waitFor } from "@testing-library/react";
import LearnPage from "@/app/languages/learn/[language]/page";
import { renderWithQueryClient } from "@/utilities/testing-helpers";
import type { FillInTheBlanksQuestion, Question } from "@/utilities/api-types";

global.fetch = jest.fn();

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
  useParams: () => ({ language: "spanish" }),
}));

describe("LearnPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("shows loading text when loading", async () => {
    mockForeverPendingFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    expect(await findByText(/loading/i)).toBeInTheDocument();
  });

  it("shows error text when there is a network error", async () => {
    mockNetworkErrorFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    expect(await findByText(/failed/i)).toBeInTheDocument();
  });

  it("shows error when request resolves unsuccessfully", async () => {
    mockServerErrorFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    expect(await findByText(/failed/i)).toBeInTheDocument();
  });

  it("shows the current question when loaded successfully", async () => {
    mockSuccessfulFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    expect(await findByText(/the cat/i)).toBeInTheDocument();
  });

  it("shows a next button when not on the last question", async () => {
    mockSuccessfulFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    expect(await findByText(/next/i)).toBeInTheDocument();
  });

  it("advances to the next question when Next button is clicked", async () => {
    mockSuccessfulFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    const nextBtn = await findByText(/next/i);
    fireEvent.click(nextBtn);

    expect(await findByText(/her coffee/i)).toBeInTheDocument();
  });

  it("shows a finish button on the last question", async () => {
    mockSuccessfulFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    await navigateToLastQuestion(findByText);

    expect(await findByText(/finish/i)).toBeInTheDocument();
  });

  it("redirects when finish button is clicked", async () => {
    mockSuccessfulFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    await navigateToLastQuestion(findByText);
    const finishBtn = await findByText(/finish/i);
    fireEvent.click(finishBtn);

    expect(mockPush).toHaveBeenCalled();
  });

  it("focuses the Next button after answer submission", async () => {
    mockSuccessfulFetch();
    const { findByRole } = renderWithQueryClient(<LearnPage />);

    const input = await findByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.keyDown(input, { key: "Enter", code: "Enter" });

    await waitFor(async () =>
      expect(await findByRole("button", { name: /next/i })).toHaveFocus()
    );
  });

  it("shows a message when no questions are available", async () => {
    mockEmptyFetch();
    const { findByText } = renderWithQueryClient(<LearnPage />);

    expect(await findByText(/no questions available/i)).toBeInTheDocument();
  });
});

const mockSuccessfulFetch = () => {
  (global.fetch as jest.Mock).mockImplementation(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve(mockQuestions),
    })
  );
};

const mockNetworkErrorFetch = () => {
  (global.fetch as jest.Mock).mockImplementation(() =>
    Promise.reject("Network Error")
  );
};

const mockServerErrorFetch = () => {
  (global.fetch as jest.Mock).mockImplementation(() =>
    Promise.resolve({
      ok: false,
      status: 500,
      statusText: "Internal Server Error",
    })
  );
};

const mockForeverPendingFetch = () => {
  (global.fetch as jest.Mock).mockImplementation(() => new Promise(() => {}));
};

const mockEmptyFetch = () => {
  (global.fetch as jest.Mock).mockImplementation(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve([]),
    })
  );
};

const mockQuestions = [
  {
    id: "1",
    questionType: "FillInTheBlanks" as const,
    text: "The cat ____ lazily on the windowsill.",
    hint: "straighten or extend one's body",
  } as FillInTheBlanksQuestion,
  {
    id: "2",
    questionType: "FillInTheBlanks" as const,
    text: "She ____ her coffee every morning.",
    hint: "to drink",
  } as FillInTheBlanksQuestion,
  {
    id: "3",
    questionType: "FillInTheBlanks" as const,
    text: "The children ____ in the park yesterday.",
    hint: "to have fun or recreation",
  } as FillInTheBlanksQuestion,
  {
    id: "4",
    questionType: "FillInTheBlanks" as const,
    text: "He ____ the piano beautifully.",
    hint: "to create music with an instrument",
  } as FillInTheBlanksQuestion,
  {
    id: "5",
    questionType: "FillInTheBlanks" as const,
    text: "They ____ dinner at 7 PM.",
    hint: "to consume food",
  } as FillInTheBlanksQuestion,
] as Question[];

async function navigateToLastQuestion(
  findByText: (text: RegExp) => Promise<HTMLElement>
) {
  for (let i = 0; i < mockQuestions.length - 1; i++) {
    const nextBtn = await findByText(/next/i);
    fireEvent.click(nextBtn);
  }
}
