import { screen, fireEvent, waitFor } from "@testing-library/react";
import {
  escapeRegex,
  renderWithQueryClient,
} from "@/utilities/testing-helpers";

import { useState } from "react";

import FillInTheBlanks from "@/app/components/challenges/FillInTheBlanks/FillInTheBlanks";
import type {
  FillInTheBlanksAttemptResponse,
  FillInTheBlanksQuestion,
} from "@/utilities/api-types";

global.fetch = jest.fn();

const mockQuestion = {
  id: "1",
  questionType: "FillInTheBlanks" as const,
  text: "The cat ____ lazily on the windowsill.",
  hint: "straighten or extend one's body",
} as FillInTheBlanksQuestion;

describe("FillInTheBlanks", () => {
  beforeEach(() => {
    (global.fetch as jest.Mock).mockClear();
  });

  it("renders the question parts and hint", () => {
    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    expect(screen.getByText(/the cat/i)).toBeInTheDocument();
    expect(screen.getByText(/on the windowsill./i)).toBeInTheDocument();
    expect(
      screen.getByText(new RegExp(escapeRegex(mockQuestion.hint)))
    ).toBeInTheDocument();
  });

  it("allows user to input an answer", () => {
    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    expect(input).toHaveValue("stretched");
  });

  it("submits the answer when Enter key is pressed", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () =>
        Promise.resolve({
          attemptStatus: "Success",
          comment: "Great job!",
          correctAnswer: "stretched",
          questionType: "FillInTheBlanks",
        } as FillInTheBlanksAttemptResponse),
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.keyDown(input, { key: "Enter", code: "Enter" });

    await waitFor(() => {
      expect(screen.getByText(/correct/i)).toBeInTheDocument();
      expect(screen.getByText(/Great job\!/)).toBeInTheDocument();
    });
  });

  it("submits the answer and shows correct feedback", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () =>
        Promise.resolve({
          attemptStatus: "Success",
          comment: "Great job!",
          correctAnswer: "stretched",
          questionType: "FillInTheBlanks",
        } as FillInTheBlanksAttemptResponse),
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.click(screen.getByText("Check"));

    await waitFor(() => {
      expect(screen.getByText(/correct/i)).toBeInTheDocument();
      expect(screen.getByText(/Great job\!/)).toBeInTheDocument();
    });
  });

  it("submits the answer and shows incorrect feedback with correct answer", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () =>
        Promise.resolve({
          attemptStatus: "Failure",
          comment: "Try again.",
          correctAnswer: "stretched",
          questionType: "FillInTheBlanks",
        } as FillInTheBlanksAttemptResponse),
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "jumped" } });
    fireEvent.click(screen.getByText("Check"));

    await waitFor(() => {
      expect(screen.getByText(/incorrect/i)).toBeInTheDocument();
      expect(screen.getByText(/Try again\./)).toBeInTheDocument();
      expect(screen.getByText(/stretched/)).toBeInTheDocument();
    });
  });

  it("submits the answer and does not show undefined feedback when there is no feedback", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () =>
        Promise.resolve({
          attemptStatus: "Failure",
          correctAnswer: "stretched",
          questionType: "FillInTheBlanks",
        } as FillInTheBlanksAttemptResponse),
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.click(screen.getByText("Check"));

    await waitFor(() => {
      expect(screen.getByText(/incorrect/i)).toBeInTheDocument();
      expect(screen.queryByText(/undefined/i)).not.toBeInTheDocument();
    });
  });

  it("submits the answer and does not show feedback when feedback is blank", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () =>
        Promise.resolve({
          attemptStatus: "Failure",
          comment: "\t\n   ",
          correctAnswer: "stretched",
          questionType: "FillInTheBlanks",
        } as FillInTheBlanksAttemptResponse),
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.click(screen.getByText("Check"));

    await waitFor(() => {
      expect(screen.getByText(/incorrect/i)).toBeInTheDocument();
      expect(screen.queryByText(new RegExp("\t\n   "))).not.toBeInTheDocument();
    });
  });

  it("does not allow checking twice", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () =>
        Promise.resolve({
          attemptStatus: "Success",
          comment: "Great job!",
          correctAnswer: "stretched",
          questionType: "FillInTheBlanks",
        } as FillInTheBlanksAttemptResponse),
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });

    const checkButton = screen.getByText("Check");
    fireEvent.click(checkButton);

    await waitFor(() => {
      expect(screen.queryByText("Check")).not.toBeInTheDocument();
    });
  });

  it("displays an error when the API request fails", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.click(screen.getByText("Check"));

    await waitFor(() => {
      expect(screen.getByText(/error/i)).toBeInTheDocument();
    });
  });

  it("displays an error when there is a network failure", async () => {
    (global.fetch as jest.Mock).mockRejectedValueOnce(
      new Error("Network error")
    );

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.click(screen.getByText("Check"));

    await waitFor(() => {
      expect(screen.getByText(/error/i)).toBeInTheDocument();
    });
  });

  it("automatically focuses the input box on render", () => {
    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    expect(input).toHaveFocus();
  });

  describe("when question changes", () => {
    const TestComponent = () => {
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

    it("should clear the input field", () => {
      renderWithQueryClient(<TestComponent />);
      const input = screen.getByRole("textbox");
      fireEvent.change(input, { target: { value: "test answer" } });
      expect(input).toHaveValue("test answer");

      fireEvent.click(screen.getByText("Change Question"));
      expect(input).toHaveValue("");
    });

    it("should make the check button visible again", async () => {
      (global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: () =>
          Promise.resolve({
            attemptStatus: "Success",
            comment: "Good job!",
            correctAnswer: "test answer",
            questionType: "FillInTheBlanks",
          } as FillInTheBlanksAttemptResponse),
      });

      renderWithQueryClient(<TestComponent />);
      const input = screen.getByRole("textbox");
      fireEvent.change(input, { target: { value: "test answer" } });
      fireEvent.click(screen.getByText("Check"));

      await waitFor(() => {
        expect(screen.queryByText("Check")).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText("Change Question"));
      expect(screen.getByText("Check")).toBeInTheDocument();
    });

    it("should clear answer status and feedback", async () => {
      (global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: () =>
          Promise.resolve({
            attemptStatus: "Success",
            comment: "Great job!",
            correctAnswer: "test answer",
            questionType: "FillInTheBlanks",
          } as FillInTheBlanksAttemptResponse),
      });

      renderWithQueryClient(<TestComponent />);
      fireEvent.change(screen.getByRole("textbox"), {
        target: { value: "test answer" },
      });
      fireEvent.click(screen.getByText("Check"));

      await waitFor(() => {
        expect(screen.getByText(/correct/i)).toBeInTheDocument();
        expect(screen.getByText(/Great job!/)).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText("Change Question"));
      expect(screen.queryByText(/correct/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Great job!/)).not.toBeInTheDocument();
    });

    it("should clear error state", async () => {
      (global.fetch as jest.Mock).mockRejectedValueOnce(
        new Error("Network error")
      );

      renderWithQueryClient(<TestComponent />);
      fireEvent.change(screen.getByRole("textbox"), {
        target: { value: "test answer" },
      });
      fireEvent.click(screen.getByText("Check"));

      await waitFor(() => {
        expect(screen.getByText(/error/i)).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText("Change Question"));
      expect(screen.queryByText(/error/i)).not.toBeInTheDocument();
    });

    it("should focus the input box when question changes", () => {
      renderWithQueryClient(<TestComponent />);
      const input = screen.getByRole("textbox");
      input.blur();
      expect(input).not.toHaveFocus();

      fireEvent.click(screen.getByText("Change Question"));
      expect(screen.getByRole("textbox")).toHaveFocus();
    });
  });
});
