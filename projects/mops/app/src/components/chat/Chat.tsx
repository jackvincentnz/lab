import { useEffect, useState } from "react";
import {
  ActionIcon,
  Alert,
  Box,
  Group,
  Loader,
  ScrollArea,
  Stack,
  Table,
  Text,
  Textarea,
  Title,
  Tooltip,
} from "@mantine/core";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {
  IconArrowRight,
  IconCheck,
  IconEdit,
  IconHistory,
  IconPlus,
  IconRefresh,
  IconX,
} from "@tabler/icons-react";
import { useMutation, useQuery } from "@apollo/client";
import {
  AddUserMessageDocument,
  AddUserMessageMutation,
  AddUserMessageMutationVariables,
  ApproveToolCallDocument,
  ApproveToolCallMutation,
  ApproveToolCallMutationVariables,
  ChatMessageStatus,
  ChatMessageType,
  EditUserMessageDocument,
  EditUserMessageMutation,
  EditUserMessageMutationVariables,
  GetChatDocument,
  GetChatQuery,
  GetChatQueryVariables,
  RejectToolCallDocument,
  RejectToolCallMutation,
  RejectToolCallMutationVariables,
  RetryAssistantMessageDocument,
  RetryAssistantMessageMutation,
  RetryAssistantMessageMutationVariables,
  StartChatDocument,
  StartChatMutation,
  StartChatMutationVariables,
  ToolCallStatus,
} from "../../__generated__/graphql";
import { ToolCallApproval } from "./ToolCallApproval";
import { ChatHistory } from "./ChatHistory";

const POLL_INTERVAL = 500;

const CHAT_VIEW = "chat";
const CHAT_HISTORY_VIEW = "history";

type ViewType = typeof CHAT_VIEW | typeof CHAT_HISTORY_VIEW;

interface MessageActionsProps {
  messageId: string;
  messageType: ChatMessageType;
  messageStatus?: ChatMessageStatus;
  content?: string;
  align: "left" | "right";
  onEdit?: (messageId: string, content: string) => void;
  onRetry?: (messageId: string) => void;
  isLoading?: boolean;
}

function MessageActions({
  messageId,
  messageType,
  messageStatus,
  content,
  align,
  onEdit,
  onRetry,
  isLoading = false,
}: MessageActionsProps) {
  const canEdit = messageType === ChatMessageType.User && content;
  const canRetry =
    messageType === ChatMessageType.Assistant &&
    (messageStatus === ChatMessageStatus.Failed ||
      messageStatus === ChatMessageStatus.Completed);

  if (!canEdit && !canRetry) {
    return null;
  }

  return (
    <Group
      gap={4}
      justify={align === "right" ? "flex-end" : "flex-start"}
      mt={4}
    >
      {canEdit && onEdit && (
        <ActionIcon
          size="sm"
          variant="subtle"
          color="gray"
          onClick={() => onEdit(messageId, content)}
          disabled={isLoading}
          aria-label="Edit message"
        >
          <IconEdit size={14} />
        </ActionIcon>
      )}
      {canRetry && onRetry && (
        <ActionIcon
          size="sm"
          variant="subtle"
          color="gray"
          onClick={() => onRetry(messageId)}
          disabled={isLoading}
          aria-label="Retry message"
        >
          <IconRefresh size={14} />
        </ActionIcon>
      )}
    </Group>
  );
}

export function Chat() {
  const [currentChatId, setCurrentChatId] = useState<string | null>(null);
  const [view, setView] = useState<ViewType>(CHAT_VIEW);
  const [input, setInput] = useState("");
  const [editingMessageId, setEditingMessageId] = useState<string | null>(null);
  const [editContent, setEditContent] = useState("");

  const reset = () => {
    setInput("");
    setEditingMessageId(null);
    setEditContent("");
  };

  const handleNewChat = () => {
    reset();
    setCurrentChatId(null);
    setView(CHAT_VIEW);
  };

  const handleShowChats = () => {
    setView(CHAT_HISTORY_VIEW);
  };

  const handleSelectChat = (selectedChatId: string) => {
    reset();
    setCurrentChatId(selectedChatId);
    setView(CHAT_VIEW);
  };

  const handleBackToChat = () => {
    setView(CHAT_VIEW);
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
        setEditingMessageId(null);
        setEditContent("");
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
        (msg) =>
          msg.type === ChatMessageType.Assistant &&
          msg.status === ChatMessageStatus.Pending,
      );

      if (!hasPendingAssistantMessages) {
        stopPolling();
      }
    }
  }, [chatData?.chat?.messages, stopPolling]);

  const sendMessage = async () => {
    const messageContent = input.trim();
    if (!messageContent) return;

    setInput("");

    if (!currentChatId) {
      await startChat({
        variables: {
          input: { content: messageContent },
        },
      });
    } else {
      await addUserMessage({
        variables: {
          input: {
            chatId: currentChatId,
            content: messageContent,
          },
        },
      });
    }
  };

  const handleEditMessage = (messageId: string, currentContent: string) => {
    setEditingMessageId(messageId);
    setEditContent(currentContent);
  };

  const handleSaveEdit = async () => {
    if (!currentChatId || !editingMessageId || !editContent.trim()) return;

    await editUserMessage({
      variables: {
        input: {
          chatId: currentChatId,
          messageId: editingMessageId,
          content: editContent.trim(),
        },
      },
    });
  };

  const handleCancelEdit = () => {
    setEditingMessageId(null);
    setEditContent("");
  };

  const handleRetryMessage = async (messageId: string) => {
    if (!currentChatId) return;

    await retryAssistantMessage({
      variables: {
        input: {
          chatId: currentChatId,
          messageId: messageId,
        },
      },
    });
  };

  const handleApproveToolCall = async (
    messageId: string,
    toolCallId: string,
  ) => {
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
  };

  const handleRejectToolCall = async (
    messageId: string,
    toolCallId: string,
  ) => {
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
  };

  const isLoading =
    startingChat ||
    addingMessage ||
    chatLoading ||
    editingMessage ||
    retryingMessage ||
    approvingToolCall ||
    rejectingToolCall;
  const messages = chatData?.chat?.messages || [];

  return (
    <Box
      p="md"
      style={{
        height: "100%",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <Group justify="space-between" align="center" mb="md">
        <Title order={3}>AI Chat</Title>

        <Group gap="xs">
          <Tooltip label="New Chat">
            <ActionIcon
              variant="subtle"
              onClick={handleNewChat}
              aria-label="New Chat"
            >
              <IconPlus size={18} />
            </ActionIcon>
          </Tooltip>
          <Tooltip label="Previous Chats">
            <ActionIcon
              variant="subtle"
              onClick={handleShowChats}
              aria-label="Previous Chats"
            >
              <IconHistory size={18} />
            </ActionIcon>
          </Tooltip>
        </Group>
      </Group>

      {view === CHAT_HISTORY_VIEW ? (
        <ChatHistory
          onSelectChat={handleSelectChat}
          onBack={handleBackToChat}
        />
      ) : (
        <>
          <ScrollArea
            style={{ flexGrow: 1, marginBottom: "1rem" }}
            scrollbarSize={6}
            offsetScrollbars
          >
            {currentChatId &&
              messages.map((msg) =>
                msg.type === ChatMessageType.User ? (
                  <Box key={msg.id} mb="md">
                    <Group justify="flex-end">
                      {editingMessageId === msg.id ? (
                        <Box style={{ maxWidth: "70%", width: "100%" }}>
                          <Textarea
                            value={editContent}
                            onChange={(e) =>
                              setEditContent(e.currentTarget.value)
                            }
                            autosize
                            minRows={2}
                            maxRows={10}
                            mb={8}
                          />
                          <Group justify="flex-end" gap={4}>
                            <ActionIcon
                              size="sm"
                              variant="filled"
                              color="green"
                              onClick={handleSaveEdit}
                              disabled={editingMessage || !editContent.trim()}
                              aria-label="Save edit"
                            >
                              <IconCheck size={14} />
                            </ActionIcon>
                            <ActionIcon
                              size="sm"
                              variant="filled"
                              color="red"
                              onClick={handleCancelEdit}
                              disabled={editingMessage}
                              aria-label="Cancel edit"
                            >
                              <IconX size={14} />
                            </ActionIcon>
                          </Group>
                        </Box>
                      ) : (
                        <Alert
                          radius="lg"
                          py={8}
                          variant="light"
                          style={{ maxWidth: "70%" }}
                        >
                          <Text size="sm" style={{ whiteSpace: "pre-line" }}>
                            {msg.content}
                          </Text>
                        </Alert>
                      )}
                    </Group>
                    {editingMessageId !== msg.id && (
                      <MessageActions
                        messageId={msg.id}
                        messageType={msg.type}
                        content={msg.content || ""}
                        align="right"
                        onEdit={handleEditMessage}
                        isLoading={isLoading}
                      />
                    )}
                  </Box>
                ) : (
                  <Box key={msg.id} mb="md">
                    {msg.status === ChatMessageStatus.Pending ? (
                      <Group align="center" gap="xs">
                        <Loader size="xs" />
                        <Text size="sm" c="dimmed">
                          Assistant is thinking...
                        </Text>
                      </Group>
                    ) : msg.status === ChatMessageStatus.Failed ? (
                      <>
                        <Alert color="red" variant="light">
                          <Text size="sm">
                            Failed to generate response. Please try again.
                          </Text>
                        </Alert>
                        <MessageActions
                          messageId={msg.id}
                          messageType={msg.type}
                          messageStatus={msg.status}
                          content={msg.content || ""}
                          align="left"
                          onRetry={handleRetryMessage}
                          isLoading={isLoading}
                        />
                      </>
                    ) : (
                      <>
                        {msg.toolCalls && msg.toolCalls.length > 0 && (
                          <Stack gap="md" mb="md">
                            {msg.toolCalls.some(
                              (tc) =>
                                tc.status === ToolCallStatus.PendingApproval,
                            ) && (
                              <Text size="sm" c="dimmed">
                                The assistant wants to make the following
                                change(s):
                              </Text>
                            )}
                            {msg.toolCalls.map((toolCall) => (
                              <ToolCallApproval
                                key={toolCall.id}
                                toolCall={toolCall}
                                onApprove={() =>
                                  handleApproveToolCall(msg.id, toolCall.id)
                                }
                                onReject={() =>
                                  handleRejectToolCall(msg.id, toolCall.id)
                                }
                              />
                            ))}
                          </Stack>
                        )}
                        {msg.content && (
                          <>
                            <ReactMarkdown
                              remarkPlugins={[remarkGfm]}
                              components={{
                                table: ({ children, ...props }) => (
                                  <Table
                                    my="md"
                                    striped
                                    highlightOnHover
                                    withTableBorder
                                    {...props}
                                  >
                                    {children}
                                  </Table>
                                ),
                                thead: ({ children, ...props }) => (
                                  <Table.Thead {...props}>
                                    {children}
                                  </Table.Thead>
                                ),
                                tbody: ({ children, ...props }) => (
                                  <Table.Tbody {...props}>
                                    {children}
                                  </Table.Tbody>
                                ),
                                tr: ({ children, ...props }) => (
                                  <Table.Tr {...props}>{children}</Table.Tr>
                                ),
                                th: ({ children, ...props }) => (
                                  <Table.Th {...props}>{children}</Table.Th>
                                ),
                                td: ({ children, ...props }) => (
                                  <Table.Td {...props}>{children}</Table.Td>
                                ),
                                p: ({ children }) => (
                                  <Text my="md">{children}</Text>
                                ),
                              }}
                            >
                              {msg.content}
                            </ReactMarkdown>
                            <MessageActions
                              messageId={msg.id}
                              messageType={msg.type}
                              messageStatus={msg.status}
                              content={msg.content || ""}
                              align="left"
                              onRetry={handleRetryMessage}
                              isLoading={isLoading}
                            />
                          </>
                        )}
                      </>
                    )}
                  </Box>
                ),
              )}
          </ScrollArea>

          <Box style={{ flexShrink: 0 }}>
            <Group gap="xs" grow>
              <Textarea
                value={input}
                onChange={(e) => setInput(e.currentTarget.value)}
                placeholder="What can you do?"
                onKeyDown={(e) =>
                  e.key === "Enter" && !e.shiftKey && sendMessage()
                }
                rightSection={
                  isLoading ? (
                    <Loader size="xs" />
                  ) : (
                    <ActionIcon onClick={sendMessage} aria-label="Send">
                      <IconArrowRight />
                    </ActionIcon>
                  )
                }
                autosize
                minRows={1}
                maxRows={10}
                rightSectionPointerEvents={"all"}
                disabled={isLoading}
              />
            </Group>
          </Box>
        </>
      )}
    </Box>
  );
}
