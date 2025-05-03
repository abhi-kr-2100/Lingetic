import LearnPageComponent from "@/app/languages/[language]/LearnPageComponent";
import { renderWithQueryClient } from "@/utilities/testing-helpers";
import type { FillInTheBlanksQuestion } from "@/utilities/api-types";

global.fetch = jest.fn();

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
  useParams: () => ({ language: "Spanish" }),
}));
jest.mock(
  "../../../app/components/questions/FillInTheBlanks/useQuestionAudioPlayback",
  () => ({
    __esModule: true,
    default: () => ({ playAudio: jest.fn() }),
  })
);

describe("LearnPageComponent", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("shows loading text when loading", async () => {
    mockForeverPendingFetch();
    const { findByText } = renderWithQueryClient(<LearnPageComponent />);

    expect(await findByText(/loading/i)).toBeInTheDocument();
  });

  it("shows error text when there is a network error", async () => {
    mockNetworkErrorFetch();
    const { findByText } = renderWithQueryClient(<LearnPageComponent />);

    expect(await findByText(/oops/i)).toBeInTheDocument();
  });

  it("shows error when request resolves unsuccessfully", async () => {
    mockServerErrorFetch();
    const { findByText } = renderWithQueryClient(<LearnPageComponent />);

    expect(await findByText(/oops/i)).toBeInTheDocument();
  });

  it("shows the current question when loaded successfully", async () => {
    mockSuccessfulFetch();
    const { findByText } = renderWithQueryClient(<LearnPageComponent />);

    expect(await findByText(/the cat/i)).toBeInTheDocument();
  });

  it("shows a message when no questions are available", async () => {
    mockEmptyFetch();
    const { findByText } = renderWithQueryClient(<LearnPageComponent />);

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
    Promise.reject(new Error("Network Error"))
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

const mockQuestions: FillInTheBlanksQuestion[] = [
  {
    id: "1",
    questionType: "FillInTheBlanks" as const,
    text: "The cat ____ lazily on the windowsill.",
    hint: "straighten or extend one's body",
    language: "Spanish",
  },
  {
    id: "2",
    questionType: "FillInTheBlanks" as const,
    text: "She ____ her coffee every morning.",
    hint: "to drink",
    language: "Spanish",
  },
  {
    id: "3",
    questionType: "FillInTheBlanks" as const,
    text: "The children ____ in the park yesterday.",
    hint: "to have fun or recreation",
    language: "Spanish",
  },
  {
    id: "4",
    questionType: "FillInTheBlanks" as const,
    text: "He ____ the piano beautifully.",
    hint: "to create music with an instrument",
    language: "Spanish",
  },
  {
    id: "5",
    questionType: "FillInTheBlanks" as const,
    text: "They ____ dinner at 7 PM.",
    hint: "to consume food",
    language: "Spanish",
  },
];
