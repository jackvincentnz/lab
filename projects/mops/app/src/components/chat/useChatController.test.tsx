import {
  act,
  beforeEach,
  describe,
  expect,
  renderHook,
  test,
  waitFor,
} from "../../test";
import { vi } from "vitest";
import {
  AddUserMessageDocument,
  ChatMessageStatus,
  ChatMessageType,
  EditUserMessageDocument,
  RetryAssistantMessageDocument,
  StartChatDocument,
} from "../../__generated__/graphql";
import { useChatController } from "./useChatController";

const mockUseQuery = vi.fn();
const mockUseMutation = vi.fn();

vi.mock("@apollo/client", () => ({
  useQuery: (...args: unknown[]) => mockUseQuery(...args),
  useMutation: (...args: unknown[]) => mockUseMutation(...args),
}));

describe("useChatController", () => {
  beforeEach(() => {
    mockUseQuery.mockReset();
    mockUseMutation.mockReset();
  });

  test("starts a new chat when sending the first message", async () => {
    const apollo = setupApollo();
    const { result } = renderHook(() => useChatController());

    act(() => {
      result.current.setInput("  hello  ");
    });

    await waitFor(() => {
      expect(result.current.input).toBe("  hello  ");
    });

    await act(async () => {
      await result.current.sendMessage();
    });

    expect(
      apollo.getCalledMutation(StartChatDocument).mutate,
    ).toHaveBeenCalledWith({
      variables: {
        input: {
          content: "hello",
        },
      },
    });
    expect(result.current.input).toBe("");

    act(() => {
      apollo.getCalledMutation(StartChatDocument).onCompleted({
        startChat: {
          success: true,
          chat: { id: "chat-1" },
        },
      });
    });

    expect(result.current.currentChatId).toBe("chat-1");
    expect(apollo.startPolling).toHaveBeenCalledWith(500);
  });

  test("adds a user message to an existing chat", async () => {
    const apollo = setupApollo();
    const { result } = renderHook(() => useChatController());

    act(() => {
      result.current.handleSelectChat("chat-1");
      result.current.setInput("next");
    });

    await waitFor(() => {
      expect(result.current.currentChatId).toBe("chat-1");
      expect(result.current.input).toBe("next");
    });

    await act(async () => {
      await result.current.sendMessage();
    });

    expect(
      apollo.getCalledMutation(AddUserMessageDocument).mutate,
    ).toHaveBeenCalledWith({
      variables: {
        input: {
          chatId: "chat-1",
          content: "next",
        },
      },
    });
  });

  test("saves an edited message for the current chat", async () => {
    const apollo = setupApollo();
    const { result } = renderHook(() => useChatController());

    act(() => {
      result.current.handleSelectChat("chat-1");
      result.current.handleEditMessage("message-1", "Original");
      result.current.setEditContent(" Updated ");
    });

    await act(async () => {
      await result.current.handleSaveEdit();
    });

    expect(
      apollo.getMutation(EditUserMessageDocument).mutate,
    ).toHaveBeenCalledWith({
      variables: {
        input: {
          chatId: "chat-1",
          messageId: "message-1",
          content: "Updated",
        },
      },
    });

    act(() => {
      apollo.getMutation(EditUserMessageDocument).onCompleted({
        editUserMessage: {
          success: true,
        },
      });
    });

    expect(result.current.editingMessageId).toBeNull();
    expect(result.current.editContent).toBe("");
  });

  test("retries assistant messages for the current chat", async () => {
    const apollo = setupApollo();
    const { result } = renderHook(() => useChatController());

    act(() => {
      result.current.handleSelectChat("chat-1");
    });

    await act(async () => {
      await result.current.handleRetryMessage("assistant-1");
    });

    expect(
      apollo.getMutation(RetryAssistantMessageDocument).mutate,
    ).toHaveBeenCalledWith({
      variables: {
        input: {
          chatId: "chat-1",
          messageId: "assistant-1",
        },
      },
    });
  });

  test("stops polling when there are no pending assistant messages", () => {
    const stopPolling = vi.fn();

    setupApollo({
      queryData: {
        chat: {
          id: "chat-1",
          messages: [
            {
              id: "assistant-1",
              type: ChatMessageType.Assistant,
              status: ChatMessageStatus.Completed,
              content: "done",
              toolCalls: [],
            },
          ],
        },
      },
      stopPolling,
    });

    renderHook(() => useChatController());

    expect(stopPolling).toHaveBeenCalled();
  });
});

function setupApollo(options?: {
  queryData?: unknown;
  stopPolling?: ReturnType<typeof vi.fn>;
}) {
  const startPolling = vi.fn();
  const stopPolling = options?.stopPolling ?? vi.fn();
  const mutations = new Map<
    unknown,
    {
      mutate: ReturnType<typeof vi.fn>;
      onCompleted: (data: unknown) => void;
    }[]
  >();

  mockUseQuery.mockReturnValue({
    data: options?.queryData,
    loading: false,
    startPolling,
    stopPolling,
  });

  mockUseMutation.mockImplementation(
    (
      _document: unknown,
      config?: { onCompleted?: (data: unknown) => void },
    ) => {
      const mutate = vi.fn().mockResolvedValue(undefined);
      const existing = mutations.get(_document) ?? [];
      existing.push({
        mutate,
        onCompleted: config?.onCompleted ?? (() => undefined),
      });
      mutations.set(_document, existing);

      return [mutate, { loading: false }];
    },
  );

  return {
    startPolling,
    stopPolling,
    getMutation(document: unknown) {
      const documentMutations = mutations.get(document);
      if (!documentMutations?.length) {
        throw new Error("Missing mutation mock");
      }

      return documentMutations[documentMutations.length - 1];
    },
    getCalledMutation(document: unknown) {
      const documentMutations = mutations.get(document);
      const calledMutation = documentMutations?.find(
        (mutation) => mutation.mutate.mock.calls.length > 0,
      );

      if (!calledMutation) {
        throw new Error("Missing called mutation mock");
      }

      return calledMutation;
    },
  };
}
