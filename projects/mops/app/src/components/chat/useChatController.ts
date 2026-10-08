import { useState } from "react";
import { useMutation, useSubscription } from "@apollo/client/react";
import {
  AddUserMessageDocument,
  ApproveToolCallDocument,
  EditUserMessageDocument,
  ChatUpdatedDocument,
  RejectToolCallDocument,
  RetryAssistantMessageDocument,
  StartChatDocument,
  type AddUserMessageMutation,
  type AddUserMessageMutationVariables,
  type ApproveToolCallMutation,
  type ApproveToolCallMutationVariables,
  type EditUserMessageMutation,
  type EditUserMessageMutationVariables,
  type ChatUpdatedSubscription,
  type ChatUpdatedSubscriptionVariables,
  type RejectToolCallMutation,
  type RejectToolCallMutationVariables,
  type RetryAssistantMessageMutation,
  type RetryAssistantMessageMutationVariables,
  type StartChatMutation,
  type StartChatMutationVariables,
} from "../../__generated__/graphql";
import { CHAT_HISTORY_VIEW, CHAT_VIEW, type ViewType } from "./chatView";

export function useChatController() {
  const [currentChatId, setCurrentChatId] = useState<string | null>(null);
  const [view, setView] = useState<ViewType>(CHAT_VIEW);
  const [input, setInput] = useState("");

  const reset = () => {
    setInput("");
  };

  const { data: chatData, loading: chatLoading } = useSubscription<
    ChatUpdatedSubscription,
    ChatUpdatedSubscriptionVariables
  >(ChatUpdatedDocument, {
    variables: { id: currentChatId || "" },
    skip: !currentChatId,
    // Subscription snapshots own the view; a slower mutation response must not overwrite them.
    fetchPolicy: "no-cache",
  });

  const [startChat, { loading: startingChat }] = useMutation<
    StartChatMutation,
    StartChatMutationVariables
  >(StartChatDocument);

  const [addUserMessage, { loading: addingMessage }] = useMutation<
    AddUserMessageMutation,
    AddUserMessageMutationVariables
  >(AddUserMessageDocument);

  const [editUserMessage, { loading: editingMessage }] = useMutation<
    EditUserMessageMutation,
    EditUserMessageMutationVariables
  >(EditUserMessageDocument);

  const [retryAssistantMessage, { loading: retryingMessage }] = useMutation<
    RetryAssistantMessageMutation,
    RetryAssistantMessageMutationVariables
  >(RetryAssistantMessageDocument);

  const [approveToolCall, { loading: approvingToolCall }] = useMutation<
    ApproveToolCallMutation,
    ApproveToolCallMutationVariables
  >(ApproveToolCallDocument);

  const [rejectToolCall, { loading: rejectingToolCall }] = useMutation<
    RejectToolCallMutation,
    RejectToolCallMutationVariables
  >(RejectToolCallDocument);

  async function sendMessage() {
    const messageContent = input.trim();
    if (!messageContent) return;

    setInput("");

    if (!currentChatId) {
      const result = await startChat({
        variables: {
          input: { content: messageContent },
        },
      });
      if (result.data?.startChat.success && result.data.startChat.chat) {
        setCurrentChatId(result.data.startChat.chat.id);
      }
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
    messages: currentChatId ? chatData?.chatUpdated?.messages || [] : [],
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
