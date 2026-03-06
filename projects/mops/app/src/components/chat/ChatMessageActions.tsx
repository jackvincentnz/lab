import { ActionIcon, Group } from "@mantine/core";
import { IconEdit, IconRefresh } from "@tabler/icons-react";
import {
  ChatMessageStatus,
  ChatMessageType,
} from "../../__generated__/graphql";

export interface ChatMessageActionsProps {
  messageId: string;
  messageType: ChatMessageType;
  messageStatus?: ChatMessageStatus;
  content?: string;
  align: "left" | "right";
  onEdit?: (messageId: string, content: string) => void;
  onRetry?: (messageId: string) => void;
  isLoading?: boolean;
}

export function ChatMessageActions({
  messageId,
  messageType,
  messageStatus,
  content,
  align,
  onEdit,
  onRetry,
  isLoading = false,
}: ChatMessageActionsProps) {
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
