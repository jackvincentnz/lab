import { MockedProvider, MockedResponse } from "@apollo/client/testing";

import { GetTasksDocument, GetTasksQuery } from "../../__generated__/graphql";
import {
  expect,
  it,
  render,
  screen,
} from "../../../../../tools/bazel/vitest/test-utils";
import DisplayTasks from "../DisplayTasks";

const title = "My Task";
const mocks: readonly MockedResponse<GetTasksQuery>[] = [
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
    <MockedProvider mocks={mocks} addTypename={false}>
      <DisplayTasks />
    </MockedProvider>,
  );

  expect(await screen.findByText(title)).toBeInTheDocument();
});
