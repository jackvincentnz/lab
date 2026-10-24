import {
  afterEach,
  describe,
  expect,
  render,
  screen,
  test,
  userEvent,
} from "../../test";
import { Shell } from "./Shell";
import { ASIDE_OPENED_KEY } from "./shellState";

describe("Shell", () => {
  afterEach(() => {
    window.sessionStorage.removeItem(ASIDE_OPENED_KEY);
  });

  test("renders the current page title", () => {
    render(
      <Shell>
        <div>Page</div>
      </Shell>,
      {
        route: "/spend",
        mockedProvider: { mocks: [] },
      },
    );

    expect(screen.getByRole("heading", { name: "Spend" })).toBeVisible();
  });

  test("tracks aside opened state in session storage", async () => {
    render(
      <Shell>
        <div>Page</div>
      </Shell>,
      {
        route: "/spend",
        mockedProvider: { mocks: [] },
      },
    );

    await userEvent.click(screen.getByRole("button", { name: "AI Chat" }));
    expect(window.sessionStorage.getItem(ASIDE_OPENED_KEY)).toBe("true");

    await userEvent.click(screen.getByRole("button", { name: "AI Chat" }));
    expect(window.sessionStorage.getItem(ASIDE_OPENED_KEY)).toBe("false");
  });
});
