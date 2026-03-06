import {
  describe,
  expect,
  render,
  screen,
  test,
  userEvent,
  vi,
} from "../../test";
import { ToolCallApproval } from "./ToolCallApproval";
import { ToolCallStatus } from "../../__generated__/graphql";

describe("ToolCallApproval", () => {
  test("calls approve and reject handlers for pending tool calls", async () => {
    const onApprove = vi.fn();
    const onReject = vi.fn();

    render(
      <ToolCallApproval
        toolCall={{
          id: "tool-call-1",
          name: "apply_patch",
          arguments: '{"path":"file.ts"}',
          status: ToolCallStatus.PendingApproval,
        }}
        onApprove={onApprove}
        onReject={onReject}
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
          name: "apply_patch",
          arguments: '{"path":"file.ts"}',
          status: ToolCallStatus.Approved,
        }}
        onApprove={() => undefined}
        onReject={() => undefined}
      />,
    );

    expect(screen.getByText("Approved")).toBeVisible();
    await userEvent.click(screen.getByRole("button", { name: /apply_patch/i }));
    expect(screen.getByText(/file.ts/i)).toBeVisible();
  });
});
