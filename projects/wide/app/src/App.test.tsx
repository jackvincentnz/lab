import type { MockLink } from "@apollo/client/testing";
import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  within,
} from "@lab/test-utils";
import {
  ProblemDocument,
  ProblemsDocument,
  SignalDocument,
  SignalsDocument,
  StrategyDocument,
} from "./__generated__/graphql";
import { App } from "./App";
import { link, problemFields, signalFields } from "./test/records";

const noSignals = {
  request: {
    query: SignalsDocument,
    variables: { page: 0, limit: 25, query: "" },
  },
  result: { data: { signals: [] } },
};

function show(route: string, mocks: MockLink.MockedResponse[] = []) {
  render(<App />, { route, mockedProvider: { mocks } });
}

describe("App", () => {
  it("opens on the signals collection", async () => {
    show("/", [noSignals]);

    expect(
      await screen.findByRole("heading", { level: 1, name: "Signals" }),
    ).toBeVisible();
    expect(screen.getByRole("link", { name: "Signals" })).toHaveAttribute(
      "aria-current",
      "page",
    );
  });

  it("explains an unknown address", () => {
    show("/unknown");

    expect(
      screen.getByText(
        "Page not found. Choose a collection from the navigation.",
      ),
    ).toBeVisible();
  });

  it("navigates between collections from the sidebar", async () => {
    show("/signals", [
      noSignals,
      {
        request: { query: ProblemsDocument, variables: { page: 0, limit: 25 } },
        result: { data: { problems: [] } },
      },
      {
        request: { query: StrategyDocument },
        result: {
          data: {
            strategy: { id: "strategy-1", content: "Grow", updatedAt: null },
          },
        },
      },
    ]);
    await screen.findByText(/^No signals yet\./);

    await userEvent.click(screen.getByRole("link", { name: "Problems" }));

    expect(await screen.findByText(/^No problems yet\./)).toBeVisible();

    await userEvent.click(screen.getByRole("link", { name: "Strategy" }));

    expect(await screen.findByText("Grow")).toBeVisible();
  });

  it("follows a linked source from a problem to its signal", async () => {
    show("/problems/problem-1", [
      {
        request: { query: ProblemDocument, variables: { id: "problem-1" } },
        result: {
          data: {
            problem: {
              ...problemFields(),
              questions: [],
              decisions: [],
              signals: [link({ signal: signalFields() }, "Observed")],
              solutions: [],
            },
          },
        },
      },
      {
        request: { query: SignalDocument, variables: { id: "signal-1" } },
        result: {
          data: { signal: { ...signalFields(), problems: [], solutions: [] } },
        },
      },
    ]);

    await userEvent.click(
      await screen.findByRole("link", { name: "Synthetic observation" }),
    );

    expect(
      await screen.findByRole("heading", { name: "Saved content" }),
    ).toBeVisible();
    expect(
      within(
        screen.getByRole("navigation", { name: "Explore WIDE" }),
      ).getByRole("link", { name: "Signals" }),
    ).toHaveAttribute("aria-current", "page");
  });

  it("opens and closes the refinement guides from the sidebar", async () => {
    show("/unknown");

    await userEvent.click(
      screen.getByRole("button", { name: "Refinement guides" }),
    );

    expect(
      await screen.findByRole("dialog", { name: "Shared refinement guides" }),
    ).toBeVisible();

    await userEvent.keyboard("{Escape}");

    await expect
      .poll(() => screen.queryByRole("dialog"))
      .not.toBeInTheDocument();
  });
});
