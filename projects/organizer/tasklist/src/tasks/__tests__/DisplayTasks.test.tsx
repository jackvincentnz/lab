import type { MockLink } from "@apollo/client/testing";

import {
  GetTasksDocument,
  type GetTasksQuery,
} from "../../__generated__/graphql";
import { expect, it, render, screen } from "@lab/test-utils";
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
