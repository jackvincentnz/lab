import { render, screen } from "@testing-library/react";
import Square from "../Square";

// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom"; // TODO: move out to common setup

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
