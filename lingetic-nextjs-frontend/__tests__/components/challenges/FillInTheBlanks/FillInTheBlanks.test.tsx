import { screen, fireEvent, waitFor } from "@testing-library/react";
import { escapeRegex, renderWithQueryClient } from "@/test-helpers/helpers";

import { useState } from "react";

import FillInTheBlanks from "@/app/components/challenges/FillInTheBlanks/FillInTheBlanks";

global.fetch = jest.fn();

const mockQuestion = {
  id: "1",
  type: "FillInTheBlanks" as const,
  text: "The cat ____ lazily on the windowsill.",
  hint: "straighten or extend one's body",
};

describe("FillInTheBlanks", () => {
  beforeEach(() => {
    (global.fetch as jest.Mock).mockClear();
  });

  it("renders the question and hint", () => {
    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    expect(
      screen.getByText(new RegExp(escapeRegex(mockQuestion.text)))
    ).toBeInTheDocument();
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

  it("submits the answer and shows correct feedback", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ status: "success", comment: "Great job!" }),
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
          status: "failure",
          comment: "Try again.",
          answer: "stretched",
        }),
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
      json: () => Promise.resolve({ status: "failure" }),
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

  it("submits the answer and does not show feedback when feedback is null", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ status: "failure", comment: null }),
    });

    renderWithQueryClient(<FillInTheBlanks question={mockQuestion} />);
    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "stretched" } });
    fireEvent.click(screen.getByText("Check"));

    await waitFor(() => {
      expect(screen.getByText(/incorrect/i)).toBeInTheDocument();
      expect(screen.queryByText(/null/i)).not.toBeInTheDocument();
    });
  });

  it("submits the answer and does not show feedback when feedback is blank", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ status: "failure", comment: "\t\n   " }),
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
      json: () => Promise.resolve({ status: "success" }),
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
        json: () => Promise.resolve({ status: "success" }),
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
          Promise.resolve({ status: "success", comment: "Great job!" }),
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
  });
});
