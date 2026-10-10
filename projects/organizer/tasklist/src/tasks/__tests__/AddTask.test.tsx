import { MantineProvider } from "@mantine/core";
import type { MockLink } from "@apollo/client/testing";
import { MockedProvider } from "@apollo/client/testing/react";
import type { ReactNode } from "react";

import {
  AddTaskDocument,
  GetTasksDocument,
  type GetTasksQuery,
} from "../../__generated__/graphql";
import {
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  waitFor,
} from "../../../../../../tools/bazel/vitest/test-utils";
import AddTask from "../AddTask";
import DisplayTasks from "../DisplayTasks";

function renderWithMocks(
  mocks: readonly MockLink.MockedResponse[],
  ui: ReactNode = <AddTask />,
) {
  render(
    <MantineProvider>
      <MockedProvider mocks={mocks}>{ui}</MockedProvider>
    </MantineProvider>,
  );
}

function addTaskMock(
  title: string,
  result?: MockLink.MockedResponse["result"],
): MockLink.MockedResponse {
  return {
    request: { query: AddTaskDocument, variables: { input: { title } } },
    result,
  };
}

function getTasksMock(
  allTasks: GetTasksQuery["allTasks"],
): MockLink.MockedResponse<GetTasksQuery> {
  return {
    request: { query: GetTasksDocument },
    result: { data: { allTasks } },
  };
}

it("submits the typed title and clears the input", async () => {
  const title = "Buy milk";
  const result = vi.fn(() => ({ data: { addTask: { id: "1" } } }));
  renderWithMocks([addTaskMock(title, result)]);

  const input = screen.getByRole("textbox", { name: "Task name" });
  await userEvent.type(input, title);
  await userEvent.click(screen.getByRole("button", { name: "Add task" }));

  await waitFor(() => expect(result).toHaveBeenCalledOnce());
  expect(input).toHaveValue("");
});

it("does not submit an empty title", async () => {
  const result = vi.fn(() => ({ data: { addTask: { id: "1" } } }));
  renderWithMocks([addTaskMock("", result)]);

  await userEvent.click(screen.getByRole("button", { name: "Add task" }));

  expect(result).not.toHaveBeenCalled();
});

it("shows an error when the mutation fails", async () => {
  const title = "Buy milk";
  renderWithMocks([{ ...addTaskMock(title), error: new Error("boom") }]);

  await userEvent.type(
    screen.getByRole("textbox", { name: "Task name" }),
    `${title}{Enter}`,
  );

  expect(await screen.findByText("Submission error!")).toBeInTheDocument();
});

it("refetches the task list after adding a task", async () => {
  const title = "Buy milk";
  renderWithMocks(
    [
      getTasksMock([]),
      addTaskMock(title, { data: { addTask: { id: "1" } } }),
      getTasksMock([{ id: "1", title, isCompleted: false }]),
    ],
    <>
      <AddTask />
      <DisplayTasks />
    </>,
  );

  await userEvent.type(
    screen.getByRole("textbox", { name: "Task name" }),
    `${title}{Enter}`,
  );

  expect(await screen.findByText(title)).toBeInTheDocument();
});
