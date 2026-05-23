import { useEffect, useState } from "react";
import { useMutation, useQuery } from "@apollo/client";
import {
  AddUserMessageDocument,
  ApproveToolCallDocument,
  ChatMessageStatus,
  ChatMessageType,
  EditUserMessageDocument,
  GetChatDocument,
  RejectToolCallDocument,
  RetryAssistantMessageDocument,
  StartChatDocument,
  type AddUserMessageMutation,
  type AddUserMessageMutationVariables,
  type ApproveToolCallMutation,
  type ApproveToolCallMutationVariables,
  type EditUserMessageMutation,
  type EditUserMessageMutationVariables,
  type GetChatQuery,
  type GetChatQueryVariables,
  type RejectToolCallMutation,
  type RejectToolCallMutationVariables,
  type RetryAssistantMessageMutation,
  type RetryAssistantMessageMutationVariables,
  type StartChatMutation,
  type StartChatMutationVariables,
} from "../../__generated__/graphql";
import { CHAT_HISTORY_VIEW, CHAT_VIEW, type ViewType } from "./chatView";

const POLL_INTERVAL = 500;

export function useChatController() {
  const [currentChatId, setCurrentChatId] = useState<string | null>(null);
  const [view, setView] = useState<ViewType>(CHAT_VIEW);
  const [input, setInput] = useState("");

  const reset = () => {
    setInput("");
  };

  const {
    data: chatData,
    loading: chatLoading,
    startPolling,
    stopPolling,
  } = useQuery<GetChatQuery, GetChatQueryVariables>(GetChatDocument, {
    variables: { id: currentChatId || "" },
    skip: !currentChatId,
    pollInterval: POLL_INTERVAL,
    fetchPolicy: "cache-and-network",
  });

  const [startChat, { loading: startingChat }] = useMutation<
    StartChatMutation,
    StartChatMutationVariables
  >(StartChatDocument, {
    // TODO: confirm if onCompleted use is a best practice, vs other async event patterns
    onCompleted: (data) => {
      if (data.startChat.success && data.startChat.chat) {
        setCurrentChatId(data.startChat.chat.id);
        startPolling(POLL_INTERVAL);
      }
    },
  });

  const [addUserMessage, { loading: addingMessage }] = useMutation<
    AddUserMessageMutation,
    AddUserMessageMutationVariables
  >(AddUserMessageDocument, {
    onCompleted: (data) => {
      if (data.addUserMessage.success) {
        startPolling(POLL_INTERVAL);
      }
    },
  });

  const [editUserMessage, { loading: editingMessage }] = useMutation<
    EditUserMessageMutation,
    EditUserMessageMutationVariables
  >(EditUserMessageDocument, {
    onCompleted: (data) => {
      if (data.editUserMessage.success) {
        startPolling(POLL_INTERVAL);
      }
    },
  });

  const [retryAssistantMessage, { loading: retryingMessage }] = useMutation<
    RetryAssistantMessageMutation,
    RetryAssistantMessageMutationVariables
  >(RetryAssistantMessageDocument, {
    onCompleted: (data) => {
      if (data.retryAssistantMessage.success) {
        startPolling(POLL_INTERVAL);
      }
    },
  });

  const [approveToolCall, { loading: approvingToolCall }] = useMutation<
    ApproveToolCallMutation,
    ApproveToolCallMutationVariables
  >(ApproveToolCallDocument, {
    onCompleted: (data) => {
      if (data.approveToolCall.success) {
        startPolling(POLL_INTERVAL);
      }
    },
  });

  const [rejectToolCall, { loading: rejectingToolCall }] = useMutation<
    RejectToolCallMutation,
    RejectToolCallMutationVariables
  >(RejectToolCallDocument, {
    onCompleted: (data) => {
      if (data.rejectToolCall.success) {
        startPolling(POLL_INTERVAL);
      }
    },
  });

  useEffect(() => {
    if (chatData?.chat?.messages) {
      const hasPendingAssistantMessages = chatData.chat.messages.some(
        (message) =>
          message.type === ChatMessageType.Assistant &&
          message.status === ChatMessageStatus.Pending,
      );

      if (!hasPendingAssistantMessages) {
        stopPolling();
      }
    }
  }, [chatData?.chat?.messages, stopPolling]);

  async function sendMessage() {
    const messageContent = input.trim();
    if (!messageContent) return;

    setInput("");

    if (!currentChatId) {
      await startChat({
        variables: {
          input: { content: messageContent },
        },
      });
      return;
    }

    await addUserMessage({
      variables: {
        input: {
          chatId: currentChatId,
          content: messageContent,
        },
      },
    });
  }

  function handleNewChat() {
    reset();
    setCurrentChatId(null);
    setView(CHAT_VIEW);
  }

  function handleShowChats() {
    setView(CHAT_HISTORY_VIEW);
  }

  function handleSelectChat(selectedChatId: string) {
    reset();
    setCurrentChatId(selectedChatId);
    setView(CHAT_VIEW);
  }

  function handleBackToChat() {
    setView(CHAT_VIEW);
  }

  async function handleSaveEdit(messageId: string, content: string) {
    const editContent = content.trim();
    if (!currentChatId || !editContent) return false;

    const result = await editUserMessage({
      variables: {
        input: {
          chatId: currentChatId,
          messageId,
          content: editContent,
        },
      },
    });

    return Boolean(result.data?.editUserMessage.success);
  }

  async function handleRetryMessage(messageId: string) {
    if (!currentChatId) return;

    await retryAssistantMessage({
      variables: {
        input: {
          chatId: currentChatId,
          messageId,
        },
      },
    });
  }

  async function handleApproveToolCall(messageId: string, toolCallId: string) {
    if (!currentChatId) return;

    await approveToolCall({
      variables: {
        input: {
          chatId: currentChatId,
          messageId,
          toolCallId,
        },
      },
    });
  }

  async function handleRejectToolCall(messageId: string, toolCallId: string) {
    if (!currentChatId) return;

    await rejectToolCall({
      variables: {
        input: {
          chatId: currentChatId,
          messageId,
          toolCallId,
        },
      },
    });
  }

  const isLoading =
    startingChat ||
    addingMessage ||
    chatLoading ||
    editingMessage ||
    retryingMessage ||
    approvingToolCall ||
    rejectingToolCall;

  return {
    currentChatId,
    view,
    input,
    setInput,
    isLoading,
    editingMessage,
    messages: chatData?.chat?.messages || [],
    handleNewChat,
    handleShowChats,
    handleSelectChat,
    handleBackToChat,
    sendMessage,
    handleSaveEdit,
    handleRetryMessage,
    handleApproveToolCall,
    handleRejectToolCall,
  };
}
