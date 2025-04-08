import { fireEvent, waitFor } from "@testing-library/react";
import {
  escapeRegex,
  renderWithQueryClient,
} from "@/utilities/testing-helpers";

import { useState } from "react";

import FillInTheBlanks from "@/app/components/questions/FillInTheBlanks/FillInTheBlanks";
import type {
  FillInTheBlanksAttemptResponse,
  FillInTheBlanksQuestion,
} from "@/utilities/api-types";

global.fetch = jest.fn();

describe("FillInTheBlanks", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders the question parts and hint", async () => {
    const { findByText } = renderWithQueryClient(
      <FillInTheBlanks question={mockQuestion} />
    );

    expect(await findByText(/the cat/i)).toBeInTheDocument();
    expect(await findByText(/on the windowsill./i)).toBeInTheDocument();
    expect(
      await findByText(new RegExp(escapeRegex(mockQuestion.hint)))
    ).toBeInTheDocument();
  });

  it("allows user to input an answer", async () => {
    const { findByRole } = renderWithQueryClient(
      <FillInTheBlanks question={mockQuestion} />
    );

    const input = await findByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });

    await waitFor(() => {
      expect(input).toHaveValue("stretched");
    });
  });

  it("submits the answer when Enter key is pressed", async () => {
    mockSuccessfulAttempt();
    const { findByRole, findByText } = renderWithQueryClient(
      <FillInTheBlanks question={mockQuestion} />
    );

    const input = await findByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.keyDown(input, { key: "Enter", code: "Enter" });

    expect(await findByText(/correct/i)).toBeInTheDocument();
  });

  it("does not allow checking twice", async () => {
    mockSuccessfulAttempt();
    const { findByRole, findByText, queryByText } = renderWithQueryClient(
      <FillInTheBlanks question={mockQuestion} />
    );

    await checkAnswer("stretched", findByRole, findByText);

    await waitFor(() => {
      expect(queryByText("Check")).not.toBeInTheDocument();
    });
  });

  it("displays an error when the API request fails", async () => {
    mockServerError();
    const { findByRole, findByText } = renderWithQueryClient(
      <FillInTheBlanks question={mockQuestion} />
    );

    await checkAnswer("stretched", findByRole, findByText);

    expect(await findByText(/error/i)).toBeInTheDocument();
  });

  it("displays an error when there is a network failure", async () => {
    mockNetworkError();
    const { findByText, findByRole } = renderWithQueryClient(
      <FillInTheBlanks question={mockQuestion} />
    );

    await checkAnswer("stretched", findByRole, findByText);

    expect(await findByText(/error/i)).toBeInTheDocument();
  });

  it("automatically focuses the input box on render", async () => {
    const { findByRole } = renderWithQueryClient(
      <FillInTheBlanks question={mockQuestion} />
    );

    const input = await findByRole("textbox");

    await waitFor(() => {
      expect(input).toHaveFocus();
    });
  });

  describe("when question changes", () => {
    it("should clear the input field", async () => {
      const { findByRole, findByText } = renderWithQueryClient(
        <ComponentWithChangeQuestionBtn />
      );

      const input = await findByRole("textbox");
      await checkAnswerAndChangeQuestion("stretched", findByRole, findByText);

      await waitFor(() => {
        expect(input).toHaveValue("");
      });
    });

    it("should make the check button visible again", async () => {
      mockSuccessfulAttempt();
      const { findByRole, findByText } = renderWithQueryClient(
        <ComponentWithChangeQuestionBtn />
      );

      await checkAnswerAndChangeQuestion("stretched", findByRole, findByText);

      expect(await findByText("Check")).toBeInTheDocument();
    });

    it("should clear answer status", async () => {
      mockSuccessfulAttempt();
      const { findByRole, findByText, queryByText } = renderWithQueryClient(
        <ComponentWithChangeQuestionBtn />
      );

      await checkAnswerAndChangeQuestion("stretched", findByRole, findByText);

      await waitFor(() => {
        expect(queryByText(/correct/i)).not.toBeInTheDocument();
      });
    });

    it("should clear error state", async () => {
      mockNetworkError();
      const { findByRole, findByText, queryByText } = renderWithQueryClient(
        <ComponentWithChangeQuestionBtn />
      );

      await checkAnswerAndChangeQuestion("stretched", findByRole, findByText);

      await waitFor(() => {
        expect(queryByText(/error/i)).not.toBeInTheDocument();
      });
    });

    it("should focus the input box when question changes", async () => {
      const { findByRole, findByText } = renderWithQueryClient(
        <ComponentWithChangeQuestionBtn />
      );

      const input = await findByRole("textbox");
      input.blur();
      const changeQuestionButton = await findByText("Change Question");
      fireEvent.click(changeQuestionButton);

      await waitFor(async () => {
        expect(await findByRole("textbox")).toHaveFocus();
      });
    });
  });
});

const mockQuestion = {
  id: "1",
  questionType: "FillInTheBlanks" as const,
  text: "The cat ____ lazily on the windowsill.",
  hint: "straighten or extend one's body",
} as FillInTheBlanksQuestion;

const mockSuccessfulAttempt = () => {
  (global.fetch as jest.Mock).mockResolvedValueOnce({
    ok: true,
    json: () =>
      Promise.resolve({
        attemptStatus: "Success",
        correctAnswer: "stretched",
        questionType: "FillInTheBlanks",
      } as FillInTheBlanksAttemptResponse),
  });
};

const mockNetworkError = () => {
  (global.fetch as jest.Mock).mockRejectedValueOnce(new Error("Network error"));
};

const mockServerError = () => {
  (global.fetch as jest.Mock).mockResolvedValueOnce({
    ok: false,
  });
};

const checkAnswer = async (
  answer: string,
  findByRole: (_role: string) => Promise<HTMLElement>,
  findByText: (_text: string | RegExp) => Promise<HTMLElement>
) => {
  const input = await findByRole("textbox");
  fireEvent.change(input, { target: { value: answer } });
  const checkButton = await findByText("Check");
  fireEvent.click(checkButton);
};

const ComponentWithChangeQuestionBtn = () => {
  const [currentQuestion, setCurrentQuestion] = useState(mockQuestion);

  return (
    <>
      <FillInTheBlanks question={currentQuestion} />
      <button
        onClick={() =>
          setCurrentQuestion({ ...mockQuestion, id: "different-id" })
        }
      >
        Change Question
      </button>
    </>
  );
};

const checkAnswerAndChangeQuestion = async (
  answer: string,
  findByRole: (_role: string) => Promise<HTMLElement>,
  findByText: (_text: string | RegExp) => Promise<HTMLElement>
) => {
  await checkAnswer(answer, findByRole, findByText);
  const changeQuestionButton = await findByText("Change Question");
  fireEvent.click(changeQuestionButton);
};
