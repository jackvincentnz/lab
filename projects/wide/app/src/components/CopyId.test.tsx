import {
  act,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
} from "@lab/test-utils";
import { CopyId } from "./CopyId";

describe("CopyId", () => {
  it("copies the id and confirms briefly", async () => {
    // Testing Library awaits a zero-delay timer that it advances only under Jest.
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup();
    const writeText = vi.spyOn(navigator.clipboard, "writeText");
    render(<CopyId id="problem-42" />);

    await user.click(screen.getByRole("button", { name: "Copy ID" }));

    expect(writeText).toHaveBeenCalledWith("problem-42");
    expect(await screen.findByRole("button", { name: "Copied" })).toBeVisible();

    act(() => vi.advanceTimersByTime(1500));

    expect(screen.getByRole("button", { name: "Copy ID" })).toBeVisible();
  });

  it("says when the clipboard refuses the write", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup();
    vi.spyOn(navigator.clipboard, "writeText").mockRejectedValue(
      new DOMException("Denied", "NotAllowedError"),
    );
    render(<CopyId id="problem-42" />);

    await user.click(screen.getByRole("button", { name: "Copy ID" }));

    expect(
      await screen.findByRole("button", { name: "Copy failed" }),
    ).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Copied" }),
    ).not.toBeInTheDocument();

    act(() => vi.advanceTimersByTime(1500));

    expect(screen.getByRole("button", { name: "Copy ID" })).toBeVisible();
  });

  it("says when no clipboard is available", async () => {
    vi.stubGlobal("navigator", { ...navigator, clipboard: undefined });
    render(<CopyId id="problem-42" />);

    await userEvent.click(screen.getByRole("button", { name: "Copy ID" }));

    expect(
      await screen.findByRole("button", { name: "Copy failed" }),
    ).toBeVisible();
  });
});
