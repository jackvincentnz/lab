import { useState } from "react";
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
import { useMutation } from "@tanstack/react-query";

interface Message {
  role: string;
  content: string;
}

interface Request {
  prompt: string;
}

interface Response {
  message: string;
}

export function Chat() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");

  const postChatMutation = useMutation({
    mutationFn: (request: Request) => {
      return fetch("/api/chats/1", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(request),
      }).then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      });
    },
    onSuccess: (response: Response) => {
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: response.message },
      ]);
    },
  });

  const sendMessage = () => {
    if (!input.trim()) return;
    const newMessages = [...messages, { role: "human", content: input }];
    setMessages(newMessages);
    postChatMutation.mutate({ prompt: input });
    setInput("");
  };

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
        {messages.map((msg, index) =>
          msg.role === "human" ? (
            <Group justify="flex-end" key={index}>
              <Alert radius="lg" py={8} variant="light">
                <Text size="sm" style={{ whiteSpace: "pre-line" }}>
                  {msg.content}
                </Text>
              </Alert>
            </Group>
          ) : (
            <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              key={index}
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
              postChatMutation.isPending ? (
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
            disabled={postChatMutation.isPending}
          />
        </Group>
      </Box>
    </Box>
  );
}
