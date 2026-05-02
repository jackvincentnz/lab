import {
  describe,
  expect,
  render,
  screen,
  test,
  userEvent,
  vi,
} from "../../test";
import {
  ChatMessageStatus,
  ChatMessageType,
  ToolCallStatus,
} from "../../__generated__/graphql";
import { ChatTranscript, type ChatTranscriptMessage } from "./ChatTranscript";

describe("ChatTranscript", () => {
  test("renders pending assistant state", () => {
    render(
      <ChatTranscript
        currentChatId="chat-1"
        messages={[
          createAssistantMessage({
            id: "assistant-1",
            status: ChatMessageStatus.Pending,
            content: null,
          }),
        ]}
        onSaveEdit={async () => false}
        onRetryMessage={() => undefined}
        onApproveToolCall={() => undefined}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    expect(screen.getByText("Assistant is thinking...")).toBeVisible();
  });

  test("retries failed assistant messages", async () => {
    const onRetryMessage = vi.fn();

    render(
      <ChatTranscript
        currentChatId="chat-1"
        messages={[
          createAssistantMessage({
            id: "assistant-1",
            status: ChatMessageStatus.Failed,
            content: null,
          }),
        ]}
        onSaveEdit={async () => false}
        onRetryMessage={onRetryMessage}
        onApproveToolCall={() => undefined}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    await userEvent.click(
      screen.getByRole("button", { name: "Retry message" }),
    );

    expect(onRetryMessage).toHaveBeenCalledWith("assistant-1");
  });

  test("renders tool calls and forwards approval actions", async () => {
    const onApproveToolCall = vi.fn();

    render(
      <ChatTranscript
        currentChatId="chat-1"
        messages={[
          createAssistantMessage({
            id: "assistant-1",
            status: ChatMessageStatus.Completed,
            content: "I will create a budget",
            toolCalls: [
              {
                id: "tool-call-1",
                name: "create_budget",
                arguments: '{"name":"new budget"}',
                status: ToolCallStatus.PendingApproval,
              },
            ],
          }),
        ]}
        onSaveEdit={async () => false}
        onRetryMessage={() => undefined}
        onApproveToolCall={onApproveToolCall}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    expect(
      screen.getByText(/the assistant wants to make the following/i),
    ).toBeVisible();
    expect(screen.getByText("I will create a budget")).toBeVisible();

    await userEvent.click(screen.getByRole("button", { name: "Approve" }));

    expect(onApproveToolCall).toHaveBeenCalledWith(
      "assistant-1",
      "tool-call-1",
    );
  });

  test("saves edited user messages", async () => {
    const onSaveEdit = vi.fn().mockResolvedValue(true);

    render(
      <ChatTranscript
        currentChatId="chat-1"
        messages={[createUserMessage({ id: "user-1", content: "Original" })]}
        onSaveEdit={onSaveEdit}
        onRetryMessage={() => undefined}
        onApproveToolCall={() => undefined}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    await userEvent.click(screen.getByRole("button", { name: "Edit message" }));
    await userEvent.clear(screen.getByRole("textbox"));
    await userEvent.type(screen.getByRole("textbox"), "Updated");
    await userEvent.click(screen.getByRole("button", { name: "Save edit" }));

    expect(onSaveEdit).toHaveBeenCalledWith("user-1", "Updated");
    expect(screen.queryByRole("textbox")).not.toBeInTheDocument();
  });

  test("keeps editing open when saving fails", async () => {
    const onSaveEdit = vi.fn().mockResolvedValue(false);

    render(
      <ChatTranscript
        currentChatId="chat-1"
        messages={[createUserMessage({ id: "user-1", content: "Original" })]}
        onSaveEdit={onSaveEdit}
        onRetryMessage={() => undefined}
        onApproveToolCall={() => undefined}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    await userEvent.click(screen.getByRole("button", { name: "Edit message" }));
    await userEvent.clear(screen.getByRole("textbox"));
    await userEvent.type(screen.getByRole("textbox"), "Updated");
    await userEvent.click(screen.getByRole("button", { name: "Save edit" }));

    expect(onSaveEdit).toHaveBeenCalledWith("user-1", "Updated");
    expect(screen.getByRole("textbox")).toHaveValue("Updated");
  });

  test("clears editing state when switching chats", async () => {
    const { rerender } = render(
      <ChatTranscript
        currentChatId="chat-1"
        messages={[createUserMessage({ id: "user-1", content: "Original" })]}
        onSaveEdit={async () => false}
        onRetryMessage={() => undefined}
        onApproveToolCall={() => undefined}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    await userEvent.click(screen.getByRole("button", { name: "Edit message" }));

    expect(screen.getByRole("textbox")).toHaveValue("Original");

    rerender(
      <ChatTranscript
        currentChatId="chat-2"
        messages={[createUserMessage({ id: "user-2", content: "Next chat" })]}
        onSaveEdit={async () => false}
        onRetryMessage={() => undefined}
        onApproveToolCall={() => undefined}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    expect(screen.queryByRole("textbox")).not.toBeInTheDocument();
    expect(screen.getByText("Next chat")).toBeVisible();
  });
});

function createUserMessage(
  overrides: Partial<ChatTranscriptMessage>,
): ChatTranscriptMessage {
  return {
    id: overrides.id ?? "user-1",
    type: ChatMessageType.User,
    status: overrides.status ?? ChatMessageStatus.Completed,
    content: overrides.content ?? "User message",
    toolCalls: overrides.toolCalls ?? [],
    createdAt: overrides.createdAt ?? new Date().toISOString(),
    updatedAt: overrides.updatedAt ?? new Date().toISOString(),
  };
}

function createAssistantMessage(
  overrides: Partial<ChatTranscriptMessage>,
): ChatTranscriptMessage {
  return {
    id: overrides.id ?? "assistant-1",
    type: ChatMessageType.Assistant,
    status: overrides.status ?? ChatMessageStatus.Completed,
    content: overrides.content ?? "Assistant reply",
    toolCalls: overrides.toolCalls ?? [],
    createdAt: overrides.createdAt ?? new Date().toISOString(),
    updatedAt: overrides.updatedAt ?? new Date().toISOString(),
  };
}
