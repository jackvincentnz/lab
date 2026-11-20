import { describe, expect, it, render, screen, vi } from "@lab/test-utils";
import { When } from "./When";

describe("When", () => {
  const now = new Date(2026, 4, 17, 9, 30);
  const twoDaysAgo = new Date(2026, 4, 15, 9, 30).toISOString();

  it("prefixes the relative time with a verb", () => {
    vi.useFakeTimers({ now });

    render(<When verb="Updated" value={twoDaysAgo} />);

    expect(screen.getByText("Updated 2 days ago")).toHaveAttribute(
      "dateTime",
      twoDaysAgo,
    );
  });

  it("capitalizes the relative time without a verb", () => {
    vi.useFakeTimers({ now });

    render(<When value={twoDaysAgo} />);

    expect(screen.getByText("2 days ago")).toBeVisible();
  });

  it("says when no time was recorded", () => {
    render(<When value={null} />);

    const time = screen.getByText("Not recorded");
    expect(time).not.toHaveAttribute("dateTime");
    expect(time).toHaveAttribute("title", "Not recorded");
  });
});
