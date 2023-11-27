import { describe, expect, it } from "vitest";
import { render, screen, userEvent } from "./test-utils";

import { Input } from "./input";

describe("Input", async () => {
  it("should render the input", () => {
    render(
      <Input
        name="email"
        type="email"
        error={undefined}
        placeholder="Email"
        label="Email Address"
        aria-label="Email Address"
      />
    );

    expect(screen.getByText("Email Address")).toBeDefined();

    expect(
      screen.getByRole("textbox", {
        name: /email address/i,
      })
    ).toBeDefined();
  });

  it("should change input value", async () => {
    render(
      <Input
        name="email"
        type="email"
        error={undefined}
        placeholder="Email"
        label="Email Address"
        aria-label="Email Address"
      />
    );

    screen.logTestingPlaygroundURL();

    const input = screen.getByRole("textbox", {
      name: /email address/i,
    });

    expect(input).toBeDefined();

    await userEvent.type(input, "1337");

    expect(input).toHaveValue("1337");
  });

  it("should render the input with error", () => {
    render(
      <Input
        name="email"
        type="email"
        placeholder="Email"
        label="Email Address"
        aria-label="Email Address"
        error="Please enter your email"
      />
    );

    expect(
      screen.getByRole("textbox", {
        name: /email address/i,
      })
    ).toBeDefined();

    expect(screen.getByRole("alert")).toHaveTextContent(
      "Please enter your email"
    );
  });
});
