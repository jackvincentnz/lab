import { render, screen } from "@testing-library/react";
import App from "../App";

// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom"; // TODO: move out to common setup

test("renders tic tac toe game", () => {
  render(<App />);
  const linkElement = screen.getByText(/Next player/i);
  expect(linkElement).toBeInTheDocument();
});
