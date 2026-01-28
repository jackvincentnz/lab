import { useQuery } from "@apollo/client";
import { Stack, Text, Paper, Group, ActionIcon, Loader } from "@mantine/core";
import { IconArrowLeft } from "@tabler/icons-react";
import { AllChatsDocument, AllChatsQuery } from "../../__generated__/graphql";

export interface ChatHistoryProps {
  onSelectChat: (chatId: string) => void;
  onBack: () => void;
}

export function ChatHistory({ onSelectChat, onBack }: ChatHistoryProps) {
  const { data, loading } = useQuery<AllChatsQuery>(AllChatsDocument);

  if (loading) {
    return <Loader />;
  }

  return (
    <Stack>
      <Group>
        <ActionIcon variant="subtle" onClick={onBack}>
          <IconArrowLeft size={18} />
        </ActionIcon>
        <Text fw={500}>Previous Chats</Text>
      </Group>
      <Stack gap="sm">
        {data?.allChats?.map((chat) => (
          <Paper
            key={chat.id}
            p="md"
            withBorder
            style={{ cursor: "pointer" }}
            onClick={() => onSelectChat(chat.id)}
          >
            <Text size="sm" lineClamp={2}>
              {chat.messages?.[0]?.content || "Empty chat"}
            </Text>
            <Text size="xs" c="dimmed">
              {chat.createdAt}
            </Text>
          </Paper>
        ))}
        {(!data?.allChats || data.allChats.length === 0) && (
          <Text c="dimmed">No previous chats</Text>
        )}
      </Stack>
    </Stack>
  );
}
