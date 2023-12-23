import { test, expect } from "@jest/globals";
import { render, screen } from "@testing-library/react";
import Square from "../Square";

test("renders Square", () => {
  render(
    <Square
      value="1"
      onClick={() => {
        // do nothing
      }}
    />
  );
  const buttonElement = screen.getByText(/1/i);
  expect(buttonElement).toBeInTheDocument();
});
