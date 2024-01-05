import { it, expect } from "@jest/globals";
import { render, screen } from "@testing-library/react";
import { MockedProvider, MockedResponse } from "@apollo/client/testing";
import { GetTasksDocument, GetTasksQuery } from "../../__generated__/graphql";
import DisplayTasks from "../DisplayTasks";

const title = "My Task";
const mocks: ReadonlyArray<MockedResponse<GetTasksQuery>> = [
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
