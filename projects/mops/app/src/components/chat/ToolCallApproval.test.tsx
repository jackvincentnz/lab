import {
  describe,
  expect,
  render,
  screen,
  test,
  userEvent,
  vi,
  waitFor,
} from "../../test";
import { ToolCallApproval } from "./ToolCallApproval";
import { ToolCallStatus } from "../../__generated__/graphql";

describe("ToolCallApproval", () => {
  test("calls reject handler for pending tool calls", async () => {
    const onReject = vi.fn();

    render(
      <ToolCallApproval
        toolCall={{
          id: "tool-call-1",
          name: "create_budget",
          arguments: '{"name":"FY26"}',
          status: ToolCallStatus.PendingApproval,
        }}
        onApprove={() => undefined}
        onReject={onReject}
      />,
    );

    await userEvent.click(screen.getByRole("button", { name: "Reject" }));

    expect(onReject).toHaveBeenCalled();
  });

  test("calls approve handler for pending tool calls", async () => {
    const onApprove = vi.fn();

    render(
      <ToolCallApproval
        toolCall={{
          id: "tool-call-1",
          name: "create_budget",
          arguments: '{"name":"FY26"}',
          status: ToolCallStatus.PendingApproval,
        }}
        onApprove={onApprove}
        onReject={() => undefined}
      />,
    );

    await userEvent.click(screen.getByRole("button", { name: "Approve" }));

    expect(onApprove).toHaveBeenCalled();
  });

  test("shows approved state for completed tool calls", async () => {
    render(
      <ToolCallApproval
        toolCall={{
          id: "tool-call-1",
          name: "create_budget",
          arguments: '{"name":"FY26"}',
          status: ToolCallStatus.Approved,
        }}
        onApprove={() => undefined}
        onReject={() => undefined}
      />,
    );

    expect(screen.getByText("Approved")).toBeVisible();

    await userEvent.click(
      screen.getByRole("button", { name: /create_budget/i }),
    );

    await waitFor(() => expect(screen.getByText(/FY26/i)).toBeVisible());
  });
});
