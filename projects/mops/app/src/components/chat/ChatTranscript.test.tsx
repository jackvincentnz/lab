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
        editingMessageId={null}
        editContent=""
        setEditContent={() => undefined}
        onEditMessage={() => undefined}
        onSaveEdit={() => undefined}
        onCancelEdit={() => undefined}
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
        editingMessageId={null}
        editContent=""
        setEditContent={() => undefined}
        onEditMessage={() => undefined}
        onSaveEdit={() => undefined}
        onCancelEdit={() => undefined}
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
            content: "Applied update",
            toolCalls: [
              {
                id: "tool-call-1",
                name: "apply_patch",
                arguments: '{"path":"file.ts"}',
                status: ToolCallStatus.PendingApproval,
              },
            ],
          }),
        ]}
        editingMessageId={null}
        editContent=""
        setEditContent={() => undefined}
        onEditMessage={() => undefined}
        onSaveEdit={() => undefined}
        onCancelEdit={() => undefined}
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
    expect(screen.getByText("Applied update")).toBeVisible();

    await userEvent.click(screen.getByRole("button", { name: "Approve" }));

    expect(onApproveToolCall).toHaveBeenCalledWith(
      "assistant-1",
      "tool-call-1",
    );
  });

  test("shows editing controls for user messages", async () => {
    const onSaveEdit = vi.fn();
    const setEditContent = vi.fn();

    render(
      <ChatTranscript
        currentChatId="chat-1"
        messages={[createUserMessage({ id: "user-1", content: "Original" })]}
        editingMessageId="user-1"
        editContent="Updated"
        setEditContent={setEditContent}
        onEditMessage={() => undefined}
        onSaveEdit={onSaveEdit}
        onCancelEdit={() => undefined}
        onRetryMessage={() => undefined}
        onApproveToolCall={() => undefined}
        onRejectToolCall={() => undefined}
        isLoading={false}
        editingMessage={false}
      />,
    );

    await userEvent.click(screen.getByRole("button", { name: "Save edit" }));

    expect(onSaveEdit).toHaveBeenCalled();
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
