import type { PropsWithChildren } from "react";
import {
  ApolloClient,
  ApolloLink,
  InMemoryCache,
  Observable,
  type Operation,
  type FetchResult,
} from "@apollo/client";
import { ApolloProvider } from "@apollo/client/react";
import { act, describe, expect, renderHook, test, waitFor } from "../../test";
import { useChatController } from "./useChatController";

function setupApollo() {
  const operations: Operation[] = [];
  const subscriptions = new Map<
    string,
    { next: (data: FetchResult) => void }
  >();
  const disposed: string[] = [];
  const client = new ApolloClient({
    cache: new InMemoryCache(),
    link: new ApolloLink(
      (operation) =>
        new Observable((observer) => {
          operations.push(operation);
          if (operation.operationName === "ChatUpdated") {
            const id = operation.variables["id"] as string;
            subscriptions.set(id, observer);
            return () => {
              subscriptions.delete(id);
              disposed.push(id);
            };
          }
          const fields: Record<string, string> = {
            StartChat: "startChat",
            AddUserMessage: "addUserMessage",
            EditUserMessage: "editUserMessage",
            RetryAssistantMessage: "retryAssistantMessage",
            ApproveToolCall: "approveToolCall",
            RejectToolCall: "rejectToolCall",
          };
          const field = fields[operation.operationName ?? ""];
          if (!field) throw new Error("Unexpected GraphQL operation");
          observer.next({
            data: {
              [field]: {
                success: true,
                code: 200,
                message: "ok",
                chat: snapshot("chat-1", "PENDING"),
              },
            },
          });
          observer.complete();
          return undefined;
        }),
    ),
  });
  const wrapper = ({ children }: PropsWithChildren) => (
    <ApolloProvider client={client}>{children}</ApolloProvider>
  );
  return {
    wrapper,
    operations,
    subscriptions,
    disposed,
    emit(id: string, status: string, content = "done") {
      subscriptions
        .get(id)
        ?.next({ data: { chatUpdated: snapshot(id, status, content) } });
    },
  };
}

function snapshot(id: string, status: string, content = "") {
  return {
    __typename: "Chat",
    id,
    createdAt: "2026-01-01",
    updatedAt: "2026-01-01",
    messages: [
      {
        __typename: "ChatMessage",
        id: `${id}-assistant`,
        type: "ASSISTANT",
        status,
        content,
        createdAt: "2026-01-01",
        updatedAt: "2026-01-01",
        toolCalls: [],
      },
    ],
  };
}

describe("useChatController", () => {
  test("starts a new chat and receives pending then completed subscription snapshots", async () => {
    const apollo = setupApollo();
    const { result } = renderHook(() => useChatController(), {
      wrapper: apollo.wrapper,
    });
    expect(apollo.operations).toHaveLength(0);
    act(() => result.current.setInput("  hello  "));
    await act(async () => {
      await result.current.sendMessage();
    });
    expect(result.current.currentChatId).toBe("chat-1");
    expect(result.current.input).toBe("");
    expect(apollo.operations[0].variables).toEqual({
      input: { content: "hello" },
    });
    await waitFor(() => expect(apollo.subscriptions.has("chat-1")).toBe(true));
    act(() => apollo.emit("chat-1", "PENDING"));
    expect(result.current.messages[0].status).toBe("PENDING");
    act(() => apollo.emit("chat-1", "COMPLETED"));
    expect(result.current.messages[0].content).toBe("done");
    // Completed chats remain subscribed for changes from other clients.
    expect(apollo.subscriptions.has("chat-1")).toBe(true);
    expect(apollo.operations.map((op) => op.operationName)).toEqual([
      "StartChat",
      "ChatUpdated",
    ]);
  });

  test("adds, edits, retries, approves and rejects without restarting or polling", async () => {
    const apollo = setupApollo();
    const { result } = renderHook(() => useChatController(), {
      wrapper: apollo.wrapper,
    });
    act(() => {
      result.current.handleSelectChat("chat-1");
      result.current.setInput("next");
    });
    await waitFor(() => expect(apollo.subscriptions.has("chat-1")).toBe(true));
    act(() => apollo.emit("chat-1", "COMPLETED", "current"));
    await act(async () => {
      await result.current.sendMessage();
      expect(await result.current.handleSaveEdit("user-1", " Updated ")).toBe(
        true,
      );
      await result.current.handleRetryMessage("assistant-1");
      await result.current.handleApproveToolCall("assistant-1", "tool-1");
      await result.current.handleRejectToolCall("assistant-1", "tool-2");
    });
    expect(apollo.operations.map((op) => op.operationName)).toEqual([
      "ChatUpdated",
      "AddUserMessage",
      "EditUserMessage",
      "RetryAssistantMessage",
      "ApproveToolCall",
      "RejectToolCall",
    ]);
    expect(apollo.operations.slice(1).map((op) => op.variables)).toEqual([
      { input: { chatId: "chat-1", content: "next" } },
      { input: { chatId: "chat-1", messageId: "user-1", content: "Updated" } },
      { input: { chatId: "chat-1", messageId: "assistant-1" } },
      {
        input: {
          chatId: "chat-1",
          messageId: "assistant-1",
          toolCallId: "tool-1",
        },
      },
      {
        input: {
          chatId: "chat-1",
          messageId: "assistant-1",
          toolCallId: "tool-2",
        },
      },
    ]);
    expect(result.current.messages[0].content).toBe("current");
    act(() => apollo.emit("chat-1", "CANCELLED"));
    expect(result.current.messages[0].status).toBe("CANCELLED");
  });

  test("switching chats, starting a new chat and unmounting dispose the old subscription", async () => {
    const apollo = setupApollo();
    const { result, unmount } = renderHook(() => useChatController(), {
      wrapper: apollo.wrapper,
    });
    act(() => result.current.handleSelectChat("chat-1"));
    await waitFor(() => expect(apollo.subscriptions.has("chat-1")).toBe(true));
    act(() => apollo.emit("chat-1", "COMPLETED"));
    act(() => result.current.handleSelectChat("chat-2"));
    await waitFor(() => expect(apollo.subscriptions.has("chat-2")).toBe(true));
    expect(apollo.disposed).toContain("chat-1");
    expect(result.current.messages).toEqual([]);
    act(() => apollo.emit("chat-2", "COMPLETED", "second chat"));
    expect(result.current.messages[0].content).toBe("second chat");
    act(() => result.current.handleNewChat());
    expect(result.current.messages).toEqual([]);
    await waitFor(() => expect(apollo.disposed).toContain("chat-2"));
    act(() => result.current.handleSelectChat("chat-3"));
    await waitFor(() => expect(apollo.subscriptions.has("chat-3")).toBe(true));
    unmount();
    await waitFor(() => expect(apollo.disposed).toContain("chat-3"));
  });
});
