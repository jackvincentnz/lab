import "@testing-library/jest-dom"; // TODO: move out to common setup
import { render, screen } from "@testing-library/react";
import { MockedProvider, MockedResponse } from "@apollo/client/testing";
import { GetTasksDocument, GetTasksQuery } from "../../__generated__/graphql";
import DisplayTasks from "../DisplayTasks";

const mocks: ReadonlyArray<MockedResponse<GetTasksQuery>> = [
  {
    request: {
      query: GetTasksDocument,
    },
    result: {
      data: {
        allTasks: [],
      },
    },
  },
];

test("renders without error", async () => {
  render(
    <MockedProvider mocks={mocks} addTypename={false}>
      <DisplayTasks />
    </MockedProvider>
  );

  expect(await screen.findByText("0")).toBeInTheDocument();
});
