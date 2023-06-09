import React from "react";
import { render, screen } from "@testing-library/react";
import Game from "../Game";

// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom"; // TODO: move out to common setup

test("renders Game", () => {
  render(<Game />);
  const goToButtonElement = screen.getByText(/Go to/i);
  expect(goToButtonElement).toBeInTheDocument();
});
