import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
} from "@lab/test-utils";
import { Sidebar } from "./Sidebar";

describe("Sidebar", () => {
  it("marks the current collection", () => {
    render(<Sidebar onOpenGuides={vi.fn()} />, {
      route: "/problems/problem-1",
    });

    expect(screen.getByRole("link", { name: "Problems" })).toHaveAttribute(
      "aria-current",
      "page",
    );
    expect(screen.getByRole("link", { name: "Signals" })).not.toHaveAttribute(
      "aria-current",
    );
  });

  it("opens the refinement guides", async () => {
    const onOpenGuides = vi.fn();
    render(<Sidebar onOpenGuides={onOpenGuides} />, { route: "/signals" });

    await userEvent.click(
      screen.getByRole("button", { name: "Refinement guides" }),
    );

    expect(onOpenGuides).toHaveBeenCalledOnce();
  });
});
