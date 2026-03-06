import { ActionIcon, Group, Title, Tooltip } from "@mantine/core";
import { IconHistory, IconPlus } from "@tabler/icons-react";

export interface ChatHeaderProps {
  onNewChat: () => void;
  onShowChats: () => void;
}

export function ChatHeader({ onNewChat, onShowChats }: ChatHeaderProps) {
  return (
    <Group justify="space-between" align="center" mb="md">
      <Title order={3}>AI Chat</Title>

      <Group gap="xs">
        <Tooltip label="New Chat">
          <ActionIcon
            variant="subtle"
            onClick={onNewChat}
            aria-label="New Chat"
          >
            <IconPlus size={18} />
          </ActionIcon>
        </Tooltip>
        <Tooltip label="Previous Chats">
          <ActionIcon
            variant="subtle"
            onClick={onShowChats}
            aria-label="Previous Chats"
          >
            <IconHistory size={18} />
          </ActionIcon>
        </Tooltip>
      </Group>
    </Group>
  );
}
