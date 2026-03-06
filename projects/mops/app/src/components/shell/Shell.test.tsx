import { describe, expect, render, screen, test, userEvent } from "../../test";
import { vi } from "vitest";
import { Shell } from "./Shell";
import { ASIDE_OPENED_KEY } from "./shellState";

vi.mock("../chat", () => ({
  Chat: () => <div>Chat Panel</div>,
}));

describe("Shell", () => {
  test("renders the current page title", () => {
    render(
      <Shell>
        <div>Page</div>
      </Shell>,
      {
        route: "/spend",
      },
    );

    expect(screen.getByRole("heading", { name: "Spend" })).toBeVisible();
  });

  test("toggles the chat aside state in session storage", async () => {
    window.sessionStorage.removeItem(ASIDE_OPENED_KEY);

    render(
      <Shell>
        <div>Page</div>
      </Shell>,
      {
        route: "/spend",
      },
    );

    await userEvent.click(screen.getByRole("button", { name: "AI Chat" }));
    expect(window.sessionStorage.getItem(ASIDE_OPENED_KEY)).toBe("true");

    await userEvent.click(screen.getByRole("button", { name: "AI Chat" }));
    expect(window.sessionStorage.getItem(ASIDE_OPENED_KEY)).toBe("false");
  });
});
