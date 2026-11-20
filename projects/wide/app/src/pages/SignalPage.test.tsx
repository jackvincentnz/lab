import type { MockLink } from "@apollo/client/testing";
import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import { SignalDocument } from "../__generated__/graphql";
import {
  candidateFields,
  link,
  problemFields,
  signalFields,
} from "../test/records";
import { SignalPage } from "./SignalPage";

function signal(record: object | null) {
  return {
    request: { query: SignalDocument, variables: { id: "signal-1" } },
    result: { data: { signal: record } },
  };
}

function show(mocks: MockLink.MockedResponse[]) {
  render(<SignalPage />, {
    route: "/signals/signal-1",
    path: "/signals/:id",
    mockedProvider: { mocks },
  });
}

describe("SignalPage", () => {
  it("shows loading until the signal arrives", async () => {
    show([signal({ ...signalFields(), problems: [], solutions: [] })]);

    expect(screen.getByRole("status")).toHaveTextContent("Loading…");
    expect(
      await screen.findByRole("heading", { name: "Synthetic observation" }),
    ).toBeVisible();
  });

  it("retries after a failure", async () => {
    show([
      {
        request: { query: SignalDocument, variables: { id: "signal-1" } },
        error: new Error("Unavailable"),
      },
      signal({ ...signalFields(), problems: [], solutions: [] }),
    ]);

    await userEvent.click(
      await screen.findByRole("button", { name: "Try again" }),
    );

    expect(
      await screen.findByRole("heading", { name: "Synthetic observation" }),
    ).toBeVisible();
  });

  it("says when the link's id is malformed", async () => {
    show([
      {
        request: { query: SignalDocument, variables: { id: "signal-1" } },
        result: {
          errors: [
            {
              message: "Malformed id: signal-1",
              extensions: { errorType: "BAD_REQUEST" },
            },
          ],
        },
      },
    ]);

    expect(await screen.findByText("Invalid signal link.")).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Try again" }),
    ).not.toBeInTheDocument();
  });

  it("says when the signal does not exist", async () => {
    show([signal(null)]);

    expect(await screen.findByText("Signal not found.")).toBeVisible();
    expect(screen.getByRole("link", { name: "Signals" })).toHaveAttribute(
      "href",
      "/signals",
    );
  });

  it("shows the source, saved content and linked records", async () => {
    show([
      signal({
        ...signalFields({ source: "Interview notes" }),
        problems: [link({ problem: problemFields() }, "Shows the need")],
        solutions: [
          link(
            { solution: candidateFields({ id: "s1", title: "Guide" }) },
            "A",
          ),
          link({ solution: candidateFields({ id: "s2", title: "Tour" }) }, "B"),
        ],
      }),
    ]);

    expect(
      await screen.findByRole("heading", { name: "Synthetic observation" }),
    ).toBeVisible();
    expect(screen.getByText("1 problem")).toBeVisible();
    expect(screen.getByText("2 candidates")).toBeVisible();
    expect(screen.getByText("Interview notes")).toBeVisible();
    expect(
      screen.getByText("  Verbatim\nwith whitespace  ", {
        normalizer: (text) => text,
      }),
    ).toBeVisible();
    expect(
      screen.getByRole("link", { name: "Finding the demo guide" }),
    ).toBeVisible();
    expect(screen.getByRole("link", { name: "Tour" })).toBeVisible();
    expect(
      screen.getByText(/Candidates may be alternatives or complementary/),
    ).toBeVisible();
  });

  it("says what is missing when nothing is linked", async () => {
    show([
      signal({
        ...signalFields({ source: null }),
        problems: [],
        solutions: [],
      }),
    ]);

    expect(await screen.findByText("No source recorded")).toBeVisible();
    expect(
      screen.getByText(
        "No problem linked yet. The need remains to be clarified.",
      ),
    ).toBeVisible();
    expect(
      screen.getByText("No candidate solutions linked yet."),
    ).toBeVisible();
  });

  it("refreshes the record on request", async () => {
    show([
      signal({ ...signalFields(), problems: [], solutions: [] }),
      signal({
        ...signalFields({ title: "Renamed" }),
        problems: [],
        solutions: [],
      }),
    ]);
    await screen.findByRole("heading", { name: "Synthetic observation" });

    await userEvent.click(screen.getByRole("button", { name: "Refresh" }));

    expect(
      await screen.findByRole("heading", { name: "Renamed" }),
    ).toBeVisible();
  });
});
