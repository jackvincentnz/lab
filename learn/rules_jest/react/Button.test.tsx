import { it, expect } from "@jest/globals";
import { render, screen } from "@testing-library/react";

import Button from "./Button";

it("renders Button", () => {
  render(<Button value="Click me!" />);

  const buttonElement = screen.getByText(/Click me!/i);

  expect(buttonElement).toBeDefined();
});
