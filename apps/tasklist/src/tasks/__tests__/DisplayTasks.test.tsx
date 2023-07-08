import "@testing-library/jest-dom"; // TODO: move out to common setup
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
        allTasks: [{ id: "123", title }],
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

  expect(await screen.findByText(title)).toBeInTheDocument();
});
