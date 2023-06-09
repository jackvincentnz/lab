import React from "react";
import { render, screen } from "@testing-library/react";
import Board from "../Board";

// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom"; // TODO: move out to common setup

test("renders Board", () => {
  render(<Board xIsNext={false} squares={[]} onPlay={() => {}} />);
  const statusDiv = screen.getByText(/Next player/i);
  expect(statusDiv).toBeInTheDocument();
});
