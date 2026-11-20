import type { MockLink } from "@apollo/client/testing";
import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import { StrategyDocument } from "../__generated__/graphql";
import { time } from "../test/records";
import { StrategyPage } from "./StrategyPage";

function strategy(content: string | null, updatedAt: string | null = time) {
  return {
    request: { query: StrategyDocument },
    result: { data: { strategy: { id: "strategy-1", content, updatedAt } } },
  };
}

function show(mocks: MockLink.MockedResponse[]) {
  render(<StrategyPage />, { route: "/strategy", mockedProvider: { mocks } });
}

describe("StrategyPage", () => {
  it("shows loading until the strategy arrives", async () => {
    show([strategy("Grow retention")]);

    expect(screen.getByRole("status")).toHaveTextContent("Loading…");
    expect(await screen.findByText("Grow retention")).toBeVisible();
  });

  it("shows the strategy with when it was updated", async () => {
    show([strategy("## Objectives\n\nGrow retention")]);

    expect(
      await screen.findByRole("heading", { name: "Objectives" }),
    ).toBeVisible();
    expect(screen.getByText(/^Updated /)).toBeVisible();
  });

  it("retries after a failure", async () => {
    show([
      { request: { query: StrategyDocument }, error: new Error("Unavailable") },
      strategy("Grow retention"),
    ]);

    await userEvent.click(
      await screen.findByRole("button", { name: "Try again" }),
    );

    expect(await screen.findByText("Grow retention")).toBeVisible();
  });

  it("says when no strategy has been set", async () => {
    show([strategy(null, null)]);

    expect(
      await screen.findByText(
        "No strategy has been set. Discuss and save it with your connected agent.",
      ),
    ).toBeVisible();
    expect(screen.queryByText(/^Updated /)).not.toBeInTheDocument();
  });

  it("refreshes the strategy on request", async () => {
    show([strategy("First draft"), strategy("Second draft")]);
    await screen.findByText("First draft");

    await userEvent.click(screen.getByRole("button", { name: "Refresh" }));

    expect(await screen.findByText("Second draft")).toBeVisible();
  });
});
