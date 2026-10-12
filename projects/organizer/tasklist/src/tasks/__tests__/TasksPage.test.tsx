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
} from "../../../../../../tools/bazel/vitest/test-utils";
import TasksPage from "../TasksPage";

it("renders the add form and the task list", async () => {
  const title = "Buy milk";
  const mocks: readonly MockLink.MockedResponse<GetTasksQuery>[] = [
    {
      request: { query: GetTasksDocument },
      result: {
        data: { allTasks: [{ id: "1", title, isCompleted: false }] },
      },
    },
  ];

  render(
    <MantineProvider>
      <MockedProvider mocks={mocks}>
        <TasksPage />
      </MockedProvider>
    </MantineProvider>,
  );

  expect(
    screen.getByRole("textbox", { name: "Task name" }),
  ).toBeInTheDocument();
  expect(await screen.findByText(title)).toBeInTheDocument();
});
