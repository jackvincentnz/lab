import type { MockLink } from "@apollo/client/testing";

import {
  GetTasksDocument,
  type GetTasksQuery,
  MarkTaskCompletedDocument,
} from "../../__generated__/graphql";
import {
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  waitFor,
} from "@lab/test-utils";
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
  render(<DisplayTasks />, { mockedProvider: { mocks } });

  expect(await screen.findByText(title)).toBeInTheDocument();
});

it("marks a clicked task completed and shows the refetched state", async () => {
  const id = "123";
  const markTaskCompleted = vi.fn(() => ({
    data: { markTaskCompleted: { id } },
  }));
  const { container } = render(<DisplayTasks />, {
    mockedProvider: {
      mocks: [
        ...mocks,
        {
          request: {
            query: MarkTaskCompletedDocument,
            variables: { input: { id } },
          },
          result: markTaskCompleted,
        },
        {
          request: { query: GetTasksDocument },
          result: {
            data: { allTasks: [{ id, title, isCompleted: true }] },
          },
        },
      ],
    },
  });

  await userEvent.click(await screen.findByText(title));

  await waitFor(() =>
    expect(
      container.querySelector(".tabler-icon-circle-check"),
    ).toBeInTheDocument(),
  );
  expect(markTaskCompleted).toHaveBeenCalledOnce();
});
