import { test, expect } from "@jest/globals";
import { render, screen } from "@testing-library/react";
import Game from "../Game";

test("renders Game", () => {
  render(<Game />);
  const goToButtonElement = screen.getByText(/Go to/i);
  expect(goToButtonElement).toBeInTheDocument();
});
