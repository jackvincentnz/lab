import {
  ActionIcon,
  Alert,
  Box,
  Group,
  Loader,
  ScrollArea,
  Stack,
  Text,
  Textarea,
} from "@mantine/core";
import { IconCheck, IconX } from "@tabler/icons-react";
import {
  ChatMessageStatus,
  ChatMessageType,
  GetChatQuery,
  ToolCallStatus,
} from "../../__generated__/graphql";
import { ToolCallApproval } from "./ToolCallApproval";
import { MarkdownContent } from "./MarkdownContent";
import { ChatMessageActions } from "./ChatMessageActions";

export type ChatTranscriptMessage = NonNullable<
  GetChatQuery["chat"]
>["messages"][number];

export interface ChatTranscriptProps {
  currentChatId: string | null;
  messages: ChatTranscriptMessage[];
  editingMessageId: string | null;
  editContent: string;
  setEditContent: (value: string) => void;
  onEditMessage: (messageId: string, currentContent: string) => void;
  onSaveEdit: () => void;
  onCancelEdit: () => void;
  onRetryMessage: (messageId: string) => void;
  onApproveToolCall: (messageId: string, toolCallId: string) => void;
  onRejectToolCall: (messageId: string, toolCallId: string) => void;
  isLoading: boolean;
  editingMessage: boolean;
}

export function ChatTranscript({
  currentChatId,
  messages,
  editingMessageId,
  editContent,
  setEditContent,
  onEditMessage,
  onSaveEdit,
  onCancelEdit,
  onRetryMessage,
  onApproveToolCall,
  onRejectToolCall,
  isLoading,
  editingMessage,
}: ChatTranscriptProps) {
  return (
    <ScrollArea
      style={{ flexGrow: 1, marginBottom: "1rem" }}
      scrollbarSize={6}
      offsetScrollbars
    >
      {currentChatId &&
        messages.map((message) =>
          message.type === ChatMessageType.User ? (
            <UserMessage
              key={message.id}
              message={message}
              editingMessageId={editingMessageId}
              editContent={editContent}
              setEditContent={setEditContent}
              onEditMessage={onEditMessage}
              onSaveEdit={onSaveEdit}
              onCancelEdit={onCancelEdit}
              isLoading={isLoading}
              editingMessage={editingMessage}
            />
          ) : (
            <AssistantMessage
              key={message.id}
              message={message}
              onRetryMessage={onRetryMessage}
              onApproveToolCall={onApproveToolCall}
              onRejectToolCall={onRejectToolCall}
              isLoading={isLoading}
            />
          ),
        )}
    </ScrollArea>
  );
}

function UserMessage({
  message,
  editingMessageId,
  editContent,
  setEditContent,
  onEditMessage,
  onSaveEdit,
  onCancelEdit,
  isLoading,
  editingMessage,
}: {
  message: ChatTranscriptMessage;
  editingMessageId: string | null;
  editContent: string;
  setEditContent: (value: string) => void;
  onEditMessage: (messageId: string, currentContent: string) => void;
  onSaveEdit: () => void;
  onCancelEdit: () => void;
  isLoading: boolean;
  editingMessage: boolean;
}) {
  return (
    <Box mb="md">
      <Group justify="flex-end">
        {editingMessageId === message.id ? (
          <Box style={{ maxWidth: "70%", width: "100%" }}>
            <Textarea
              value={editContent}
              onChange={(event) => setEditContent(event.currentTarget.value)}
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
                onClick={onSaveEdit}
                disabled={editingMessage || !editContent.trim()}
                aria-label="Save edit"
              >
                <IconCheck size={14} />
              </ActionIcon>
              <ActionIcon
                size="sm"
                variant="filled"
                color="red"
                onClick={onCancelEdit}
                disabled={editingMessage}
                aria-label="Cancel edit"
              >
                <IconX size={14} />
              </ActionIcon>
            </Group>
          </Box>
        ) : (
          <Alert radius="lg" py={8} variant="light" style={{ maxWidth: "70%" }}>
            <Text size="sm" style={{ whiteSpace: "pre-line" }}>
              {message.content}
            </Text>
          </Alert>
        )}
      </Group>
      {editingMessageId !== message.id && (
        <ChatMessageActions
          messageId={message.id}
          messageType={message.type}
          content={message.content || ""}
          align="right"
          onEdit={onEditMessage}
          isLoading={isLoading}
        />
      )}
    </Box>
  );
}

function AssistantMessage({
  message,
  onRetryMessage,
  onApproveToolCall,
  onRejectToolCall,
  isLoading,
}: {
  message: ChatTranscriptMessage;
  onRetryMessage: (messageId: string) => void;
  onApproveToolCall: (messageId: string, toolCallId: string) => void;
  onRejectToolCall: (messageId: string, toolCallId: string) => void;
  isLoading: boolean;
}) {
  return (
    <Box mb="md">
      {message.status === ChatMessageStatus.Pending ? (
        <Group align="center" gap="xs">
          <Loader size="xs" />
          <Text size="sm" c="dimmed">
            Assistant is thinking...
          </Text>
        </Group>
      ) : message.status === ChatMessageStatus.Failed ? (
        <>
          <Alert color="red" variant="light">
            <Text size="sm">
              Failed to generate response. Please try again.
            </Text>
          </Alert>
          <ChatMessageActions
            messageId={message.id}
            messageType={message.type}
            messageStatus={message.status}
            content={message.content || ""}
            align="left"
            onRetry={onRetryMessage}
            isLoading={isLoading}
          />
        </>
      ) : (
        <>
          {message.toolCalls && message.toolCalls.length > 0 && (
            <Stack gap="md" mb="md">
              {message.toolCalls.some(
                (toolCall) =>
                  toolCall.status === ToolCallStatus.PendingApproval,
              ) && (
                <Text size="sm" c="dimmed">
                  The assistant wants to make the following change(s):
                </Text>
              )}
              {message.toolCalls.map((toolCall) => (
                <ToolCallApproval
                  key={toolCall.id}
                  toolCall={toolCall}
                  onApprove={() => onApproveToolCall(message.id, toolCall.id)}
                  onReject={() => onRejectToolCall(message.id, toolCall.id)}
                />
              ))}
            </Stack>
          )}
          {message.content && (
            <>
              <MarkdownContent content={message.content} />
              <ChatMessageActions
                messageId={message.id}
                messageType={message.type}
                messageStatus={message.status}
                content={message.content || ""}
                align="left"
                onRetry={onRetryMessage}
                isLoading={isLoading}
              />
            </>
          )}
        </>
      )}
    </Box>
  );
}
