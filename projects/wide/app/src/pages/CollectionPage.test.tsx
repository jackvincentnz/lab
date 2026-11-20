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
  ProblemsDocument,
  SignalsDocument,
  SolutionsDocument,
} from "../__generated__/graphql";
import { kinds, type Kind } from "../lib/kinds";
import { time } from "../test/records";
import { CollectionPage } from "./CollectionPage";

const documents = {
  signals: SignalsDocument,
  problems: ProblemsDocument,
  solutions: SolutionsDocument,
};

/** Rows shaped like each kind's list query; the first `linkedCount` rows are linked. */
function rows(kind: Kind, count: number, linkedCount = 0) {
  return Array.from({ length: count }, (_, i) => {
    const links = (__typename: string) =>
      i < linkedCount ? [{ __typename, id: `link-${i}` }] : [];
    const row = { id: `${kind}-${i}`, title: `Row ${i}` };
    if (kind === "signals") {
      return {
        __typename: "Signal",
        ...row,
        source: i % 2 ? "Synthetic fixture" : null,
        capturedAt: time,
        problems: links("ProblemSignal"),
        solutions: [],
      };
    }
    if (kind === "problems") {
      return {
        __typename: "Problem",
        ...row,
        updatedAt: time,
        signals: links("ProblemSignal"),
        solutions: [],
      };
    }
    return {
      __typename: "Solution",
      ...row,
      updatedAt: time,
      signals: [],
      problems: links("SolutionProblem"),
    };
  });
}

interface ListOptions {
  page?: number;
  query?: string;
}

function list(
  kind: Kind,
  entries: object[],
  { page = 0, query = "" }: ListOptions = {},
): MockLink.MockedResponse {
  const variables =
    kind === "signals" ? { page, limit: 25, query } : { page, limit: 25 };
  return {
    request: { query: documents[kind], variables },
    result: { data: { [kind]: entries } },
  };
}

async function search(text: string) {
  const box = screen.getByRole("textbox", { name: "Search signals" });

  await userEvent.clear(box);
  await userEvent.type(box, `${text}{enter}`);
}

function show(kind: Kind, mocks: MockLink.MockedResponse[], search = "") {
  render(<CollectionPage kind={kind} />, {
    route: `/${kind}${search}`,
    path: `/${kind}`,
    mockedProvider: { mocks },
  });
}

describe("CollectionPage", () => {
  it("shows loading until the collection arrives", async () => {
    show("signals", [list("signals", rows("signals", 1))]);

    expect(screen.getByRole("status")).toHaveTextContent("Loading…");
    expect(await screen.findByText("Row 0")).toBeVisible();
  });

  it("retries after a failure", async () => {
    show("problems", [
      {
        request: { query: ProblemsDocument, variables: { page: 0, limit: 25 } },
        error: new Error("Unavailable"),
      },
      list("problems", rows("problems", 1)),
    ]);

    await userEvent.click(
      await screen.findByRole("button", { name: "Try again" }),
    );

    expect(await screen.findByText("Row 0")).toBeVisible();
  });

  it.each([
    ["signals", "No signals yet."],
    ["problems", "No problems yet."],
    ["solutions", "No candidates yet."],
  ] as const)("says when there are no %s", async (kind, message) => {
    show(kind, [list(kind, [])]);

    expect(
      await screen.findByText(
        `${message} Use your connected agent to capture or refine an opportunity.`,
      ),
    ).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Next" }),
    ).not.toBeInTheDocument();
  });

  it("lists signals with their source, links and capture time", async () => {
    show("signals", [list("signals", rows("signals", 2, 1))]);

    const first = await screen.findByRole("link", { name: /Row 0/ });
    const second = screen.getByRole("link", { name: /Row 1/ });

    expect(first).toHaveAttribute("href", "/signals/signals-0");
    expect(within(first).getByText("No source")).toBeVisible();
    expect(within(first).getByText("1 problem")).toBeVisible();
    expect(within(second).getByText("Synthetic fixture")).toBeVisible();
    expect(within(second).getByText("Not yet refined")).toBeVisible();
    expect(screen.getByText("Captured")).toBeInTheDocument();
    expect(screen.getByRole("search")).toBeVisible();
  });

  it("lists problems with their links and update time", async () => {
    show("problems", [list("problems", rows("problems", 1, 1))]);

    const row = await screen.findByRole("link", { name: /Row 0/ });

    expect(row).toHaveAttribute("href", "/problems/problems-0");
    expect(within(row).getByText("1 source")).toBeVisible();
    expect(screen.getByText("Updated")).toBeInTheDocument();
    expect(screen.queryByRole("search")).not.toBeInTheDocument();
  });

  it("flags candidates without a linked problem", async () => {
    show("solutions", [list("solutions", rows("solutions", 1))]);

    const row = await screen.findByRole("link", { name: /Row 0/ });

    expect(row).toHaveAttribute("href", "/solutions/solutions-0");
    expect(within(row).getByText("No problem linked")).toBeVisible();
  });

  it.each(kinds)("pages through full pages of %s", async (kind) => {
    show(kind, [
      list(kind, rows(kind, 25, 25)),
      list(kind, rows(kind, 1, 1), { page: 1 }),
      list(kind, rows(kind, 25, 25)),
    ]);
    await screen.findByText("Row 24");

    expect(screen.getByRole("button", { name: "Previous" })).toBeDisabled();

    await userEvent.click(screen.getByRole("button", { name: "Next" }));

    expect(await screen.findByText(/· page 2$/)).toBeVisible();
    expect(screen.queryByText("Row 24")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Next" })).toBeDisabled();

    await userEvent.click(screen.getByRole("button", { name: "Previous" }));

    expect(await screen.findByText("Row 24")).toBeVisible();
    expect(screen.queryByText(/· page 2$/)).not.toBeInTheDocument();
  });

  it("says when a later page is empty", async () => {
    show("signals", [list("signals", [], { page: 3 })], "?page=3");

    expect(
      await screen.findByText(
        "No more records on this page. Return to the previous page.",
      ),
    ).toBeVisible();
    expect(screen.getByRole("button", { name: "Previous" })).toBeEnabled();
  });

  it.each(["-1", "1.5", "abc", "2147483648"])(
    "starts at the first page for page %j",
    async (page) => {
      show("signals", [list("signals", rows("signals", 1))], `?page=${page}`);

      expect(await screen.findByText("Row 0")).toBeVisible();
    },
  );

  it("searches signals and clears the search", async () => {
    show("signals", [
      list("signals", rows("signals", 25)),
      list("signals", [], { query: "absent" }),
      list("signals", rows("signals", 25)),
    ]);
    await screen.findByText("Row 24");

    await search("absent");

    expect(
      await screen.findByText("No signals match this search."),
    ).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Previous" }),
    ).not.toBeInTheDocument();

    await search("");

    expect(await screen.findByText("Row 24")).toBeVisible();
  });

  it("returns to the first page for a new search and ignores an old filter", async () => {
    show(
      "signals",
      [
        list("signals", rows("signals", 1), { page: 2 }),
        list("signals", [], { query: "absent" }),
      ],
      "?page=2&filter=unlinked",
    );
    await screen.findByText("Row 0");

    await search("absent");

    expect(
      await screen.findByText("No signals match this search."),
    ).toBeVisible();
  });

  it("refreshes the collection on request", async () => {
    show("signals", [
      list("signals", rows("signals", 1)),
      list("signals", rows("signals", 2)),
    ]);
    await screen.findByText("Row 0");

    await userEvent.click(screen.getByRole("button", { name: "Refresh" }));

    expect(await screen.findByText("Row 1")).toBeVisible();
  });
});
