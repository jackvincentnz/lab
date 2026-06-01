import { MantineProvider } from "@mantine/core";
import type { MockLink } from "@apollo/client/testing";
import { MockedProvider } from "@apollo/client/testing/react";

import {
  GetTasksDocument,
  type GetTasksQuery,
} from "../../__generated__/graphql";
import {
  expect,
  it,
  render,
  screen,
} from "../../../../../../tools/bazel/vitest/test-utils"; // FIXME: setup @lab/test-utils package to avoid relative jungle
import DisplayTasks from "../DisplayTasks";

const title = "My Task";
const mocks: readonly MockLink.MockedResponse<GetTasksQuery>[] = [
  {
    request: {
      query: GetTasksDocument,
    },
    result: {
      data: {
        allTasks: [{ id: "123", title, isCompleted: false }],
      },
    },
  },
];

it("renders without error", async () => {
  render(
    // FIXME: move providers into custom render
    <MantineProvider>
      <MockedProvider mocks={mocks}>
        <DisplayTasks />
      </MockedProvider>
    </MantineProvider>,
  );

  expect(await screen.findByText(title)).toBeInTheDocument();
});
