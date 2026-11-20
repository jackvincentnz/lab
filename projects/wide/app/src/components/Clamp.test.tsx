import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import { Clamp } from "./Clamp";

describe("Clamp", () => {
  it("shows short text without a toggle", () => {
    render(<Clamp limit={10}>Short text</Clamp>);

    expect(screen.getByText("Short text")).toBeInTheDocument();
    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("expands and collapses text longer than the limit", async () => {
    render(<Clamp limit={10}>Longer than ten characters</Clamp>);
    const toggle = screen.getByRole("button", { name: "Show all" });

    await userEvent.click(toggle);

    expect(toggle).toHaveAccessibleName("Show less");
    expect(toggle).toHaveAttribute("aria-expanded", "true");

    await userEvent.click(toggle);

    expect(toggle).toHaveAccessibleName("Show all");
    expect(toggle).toHaveAttribute("aria-expanded", "false");
  });

  it("clamps at 240 characters by default", () => {
    render(<Clamp>{"x".repeat(241)}</Clamp>);

    expect(screen.getByRole("button", { name: "Show all" })).toBeVisible();
  });
});
