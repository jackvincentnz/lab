import { useQuery } from "@apollo/client";
import {
  Stack,
  Text,
  Paper,
  Group,
  ActionIcon,
  Loader,
  Alert,
} from "@mantine/core";
import { IconArrowLeft, IconExclamationCircle } from "@tabler/icons-react";
import { AllChatsDocument, AllChatsQuery } from "../../__generated__/graphql";
import dayjs from "dayjs";

export interface ChatHistoryProps {
  onSelectChat: (chatId: string) => void;
  onBack: () => void;
}

export function ChatHistory({ onSelectChat, onBack }: ChatHistoryProps) {
  const { data, loading, error } = useQuery<AllChatsQuery>(AllChatsDocument);

  if (loading) {
    return <Loader />;
  }

  if (error) {
    return (
      <Alert
        variant="light"
        color="red"
        title="Error"
        icon={<IconExclamationCircle />}
      >
        Something went wrong. Please try again.
      </Alert>
    );
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
              {dayjs(chat.createdAt).format("MMM D, YYYY h:mm A")}
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
