import { MantineProvider } from "@mantine/core";
import type { MockLink } from "@apollo/client/testing";
import { MockedProvider } from "@apollo/client/testing/react";

import {
  GetEntriesDocument,
  type GetEntriesQuery,
} from "../../__generated__/graphql";
import {
  expect,
  it,
  render,
  screen,
} from "../../../../../../tools/bazel/vitest/test-utils";
import { EntriesPage } from "../EntriesPage";

function renderPage(allEntries: GetEntriesQuery["allEntries"]) {
  const mocks: readonly MockLink.MockedResponse<GetEntriesQuery>[] = [
    {
      request: { query: GetEntriesDocument },
      result: { data: { allEntries } },
      // The page polls, so later requests must still find a response.
      maxUsageCount: Number.POSITIVE_INFINITY,
    },
  ];

  return render(
    <MantineProvider>
      <MockedProvider mocks={mocks}>
        <EntriesPage />
      </MockedProvider>
    </MantineProvider>,
  );
}

it("renders each entry's message as its title and body with its time", async () => {
  const message = "Wrote some tests";
  const createdAt = "2026-10-09T10:00:00Z";
  renderPage([{ id: "1", message, createdAt }]);

  expect(await screen.findAllByText(message)).toHaveLength(2);
  expect(screen.getByText(createdAt)).toBeInTheDocument();
});

it("renders an empty timeline while entries are loading", () => {
  const message = "Not loaded yet";
  const { container } = renderPage([{ id: "1", message, createdAt: "now" }]);

  expect(
    container.querySelector(".mantine-Timeline-root"),
  ).toBeEmptyDOMElement();
  expect(screen.queryByText(message)).not.toBeInTheDocument();
});
