import { test, expect } from "@jest/globals";
import { render, screen } from "@testing-library/react";
import Board from "../Board";

test("renders Board", () => {
  render(
    <Board
      xIsNext={false}
      squares={[]}
      onPlay={() => {
        // do nothing
      }}
    />,
  );
  const statusDiv = screen.getByText(/Next player/i);
  expect(statusDiv).toBeInTheDocument();
});
