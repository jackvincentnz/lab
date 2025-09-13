import { useState, useEffect } from "react";
import {
  ActionIcon,
  Alert,
  Box,
  Group,
  Loader,
  ScrollArea,
  Table,
  Text,
  Textarea,
  Title,
} from "@mantine/core";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { IconArrowRight } from "@tabler/icons-react";
import { useMutation, useQuery } from "@apollo/client";
import {
  ChatMessageType,
  ChatMessageStatus,
  StartChatDocument,
  StartChatMutation,
  StartChatMutationVariables,
  AddUserMessageDocument,
  AddUserMessageMutation,
  AddUserMessageMutationVariables,
  GetChatDocument,
  GetChatQuery,
  GetChatQueryVariables,
} from "../../__generated__/graphql";

const POLL_INTERVAL = 500;

export function Chat() {
  const [currentChatId, setCurrentChatId] = useState<string | null>(null);
  const [input, setInput] = useState("");

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

  const isLoading = startingChat || addingMessage || chatLoading;
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
      <Title order={3} mb="md">
        AI Chat
      </Title>

      <ScrollArea
        style={{ flexGrow: 1, marginBottom: "1rem" }}
        scrollbarSize={6}
        offsetScrollbars
      >
        {messages.map((msg) =>
          msg.type === ChatMessageType.User ? (
            <Group justify="flex-end" key={msg.id}>
              <Alert radius="lg" py={8} variant="light">
                <Text size="sm" style={{ whiteSpace: "pre-line" }}>
                  {msg.content}
                </Text>
              </Alert>
            </Group>
          ) : (
            <Box key={msg.id}>
              {msg.status === ChatMessageStatus.Pending ? (
                <Group align="center" gap="xs">
                  <Loader size="xs" />
                  <Text size="sm" c="dimmed">
                    Assistant is thinking...
                  </Text>
                </Group>
              ) : msg.status === ChatMessageStatus.Failed ? (
                <Alert color="red" variant="light">
                  <Text size="sm">
                    Failed to generate response. Please try again.
                  </Text>
                </Alert>
              ) : (
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
                      <Table.Thead {...props}>{children}</Table.Thead>
                    ),
                    tbody: ({ children, ...props }) => (
                      <Table.Tbody {...props}>{children}</Table.Tbody>
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
                    p: ({ children }) => <Text my="md">{children}</Text>,
                  }}
                >
                  {msg.content}
                </ReactMarkdown>
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
            onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && sendMessage()}
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
    </Box>
  );
}
