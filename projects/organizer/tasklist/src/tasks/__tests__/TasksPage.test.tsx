import { MantineProvider } from "@mantine/core";

import {
  afterEach,
  expect,
  it,
  render,
  screen,
  vi,
} from "../../../../../../tools/bazel/vitest/test-utils";
import TasksPage from "../TasksPage";

afterEach(() => {
  vi.unstubAllGlobals();
});

it("renders the add form and the task list", async () => {
  const title = "Buy milk";
  // TasksPage builds its own HttpLink client, so the test stubs fetch instead of using MockedProvider.
  const fetch = vi.fn(
    async () =>
      new Response(
        JSON.stringify({
          data: {
            allTasks: [
              { __typename: "Task", id: "1", title, isCompleted: false },
            ],
          },
        }),
        { headers: { "content-type": "application/json" } },
      ),
  );
  vi.stubGlobal("fetch", fetch);

  render(
    <MantineProvider>
      <TasksPage />
    </MantineProvider>,
  );

  expect(
    screen.getByRole("textbox", { name: "Task name" }),
  ).toBeInTheDocument();
  expect(await screen.findByText(title)).toBeInTheDocument();
  expect(fetch).toHaveBeenCalledWith("/graphql", expect.anything());
});
