import { InMemoryCache } from "@apollo/client";
import type { MockLink } from "@apollo/client/testing";
import { MockedProvider } from "@apollo/client/testing/react";
import type { PropsWithChildren } from "react";
import {
  act,
  afterEach,
  beforeEach,
  describe,
  expect,
  renderHook,
  test,
  vi,
} from "../../test";
import {
  AddUserMessageDocument,
  EditUserMessageDocument,
  GetChatDocument,
  RetryAssistantMessageDocument,
  StartChatDocument,
  type ChatMessageStatus,
} from "../../__generated__/graphql";
import { useChatController } from "./useChatController";

const CHAT_ID = "chat-1";
const TIMESTAMP = "2026-03-01T12:00:00.000Z";
const POLL_INTERVAL = 500;

describe("useChatController", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  test("starts a new chat and polls for the reply", async () => {
    const startChat = mockMutation(
      StartChatDocument,
      { input: { content: "hello" } },
      "startChat",
    );
    const { result } = renderChatController([
      startChat.mock,
      mockGetChat("PENDING"),
      mockGetChat("COMPLETED"),
    ]);

    act(() => {
      result.current.setInput("  hello  ");
    });

    await runAndWaitForPoll(() => result.current.sendMessage());

    expect(startChat.result).toHaveBeenCalledOnce();
    expect(result.current.input).toBe("");
    expect(result.current.currentChatId).toBe(CHAT_ID);
    expect(result.current.messages[1]?.status).toBe("COMPLETED");
  });

  test("adds a user message and polls for the reply", async () => {
    const addUserMessage = mockMutation(
      AddUserMessageDocument,
      { input: { chatId: CHAT_ID, content: "next" } },
      "addUserMessage",
    );
    const reply = mockGetChat("COMPLETED");
    const { result } = renderChatController([
      mockGetChat("COMPLETED"),
      addUserMessage.mock,
      mockGetChat("PENDING"),
      reply,
    ]);
    await selectChat(result);

    act(() => {
      result.current.setInput("next");
    });

    await runAndWaitForPoll(() => result.current.sendMessage());

    expect(addUserMessage.result).toHaveBeenCalledOnce();
    expect(reply.result).toHaveBeenCalledOnce();
  });

  test("saves an edited message and polls for the reply", async () => {
    const editUserMessage = mockMutation(
      EditUserMessageDocument,
      {
        input: { chatId: CHAT_ID, messageId: "message-1", content: "Updated" },
      },
      "editUserMessage",
    );
    const reply = mockGetChat("COMPLETED");
    const { result } = renderChatController([
      mockGetChat("COMPLETED"),
      editUserMessage.mock,
      mockGetChat("PENDING"),
      reply,
    ]);
    await selectChat(result);

    const saved = await runAndWaitForPoll(() =>
      result.current.handleSaveEdit("message-1", " Updated "),
    );

    expect(saved).toBe(true);
    expect(editUserMessage.result).toHaveBeenCalledOnce();
    expect(reply.result).toHaveBeenCalledOnce();
  });

  test("retries an assistant message and polls for the reply", async () => {
    const retryAssistantMessage = mockMutation(
      RetryAssistantMessageDocument,
      { input: { chatId: CHAT_ID, messageId: "assistant-1" } },
      "retryAssistantMessage",
    );
    const reply = mockGetChat("COMPLETED");
    const { result } = renderChatController([
      mockGetChat("COMPLETED"),
      retryAssistantMessage.mock,
      mockGetChat("PENDING"),
      reply,
    ]);
    await selectChat(result);

    await runAndWaitForPoll(() =>
      result.current.handleRetryMessage("assistant-1"),
    );

    expect(retryAssistantMessage.result).toHaveBeenCalledOnce();
    expect(reply.result).toHaveBeenCalledOnce();
  });

  test("polls the chat until the assistant message completes", async () => {
    const { result } = renderChatController([
      mockGetChat("PENDING"),
      mockGetChat("COMPLETED"),
    ]);

    await selectChat(result);
    await act(() => vi.advanceTimersByTimeAsync(POLL_INTERVAL));

    expect(result.current.messages[1]?.status).toBe("COMPLETED");
  });

  test("stops polling when there are no pending assistant messages", async () => {
    const getChat = {
      ...mockGetChat("COMPLETED"),
      maxUsageCount: Number.POSITIVE_INFINITY,
    };
    const { result } = renderChatController([getChat]);

    await selectChat(result);
    await act(() => vi.advanceTimersByTimeAsync(POLL_INTERVAL * 2));

    expect(result.current.messages).toHaveLength(2);
    expect(getChat.result).toHaveBeenCalledOnce();
  });
});

type ChatController = ReturnType<typeof renderChatController>["result"];

async function selectChat(result: ChatController) {
  act(() => {
    result.current.handleSelectChat(CHAT_ID);
  });
  await act(() => vi.advanceTimersByTimeAsync(POLL_INTERVAL));
}

// Mock responses arrive after a short delay; a new chat id only starts its query after the
// re-render that ends the first act, so the poll needs a second advance.
async function runAndWaitForPoll<T>(action: () => Promise<T>) {
  let value!: T;
  await act(async () => {
    const pending = action();
    await vi.advanceTimersByTimeAsync(POLL_INTERVAL);
    value = await pending;
  });
  await act(() => vi.advanceTimersByTimeAsync(POLL_INTERVAL * 2));

  return value;
}

function renderChatController(mocks: MockLink.MockedResponse[]) {
  return renderHook(() => useChatController(), {
    wrapper: ({ children }: PropsWithChildren) => (
      <MockedProvider cache={new InMemoryCache()} mocks={mocks}>
        {children}
      </MockedProvider>
    ),
  });
}

function mockMutation(
  query: MockLink.MockedResponse["request"]["query"],
  variables: Record<string, unknown>,
  field: string,
) {
  const result = vi.fn(() => ({
    data: {
      [field]: {
        code: "200",
        success: true,
        message: "ok",
        chat: {
          id: CHAT_ID,
          messages: [],
          createdAt: TIMESTAMP,
          updatedAt: TIMESTAMP,
        },
      },
    },
  }));
  const mock: MockLink.MockedResponse = {
    request: { query, variables },
    result,
  };

  return { mock, result };
}

function mockGetChat(assistantStatus: ChatMessageStatus) {
  const result = vi.fn(() => ({
    data: {
      chat: {
        id: CHAT_ID,
        messages: [
          message("message-1", "USER", "COMPLETED"),
          message("assistant-1", "ASSISTANT", assistantStatus),
        ],
        createdAt: TIMESTAMP,
        updatedAt: TIMESTAMP,
      },
    },
  }));

  return {
    request: { query: GetChatDocument, variables: { id: CHAT_ID } },
    result,
  } satisfies MockLink.MockedResponse;
}

function message(id: string, type: string, status: ChatMessageStatus) {
  return {
    id,
    type,
    status,
    content: id,
    toolCalls: [],
    createdAt: TIMESTAMP,
    updatedAt: TIMESTAMP,
  };
}
