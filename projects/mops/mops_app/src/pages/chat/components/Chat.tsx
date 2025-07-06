import { useState } from "react";
import {
  ActionIcon,
  Box,
  Container,
  Group,
  ScrollArea,
  Stack,
  TextInput,
  Loader,
  Table,
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
      return fetch("/chats/1", {
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
    <Container>
      <Stack>
        <ScrollArea>
          {messages.map((msg, index) => (
            <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              key={index}
              components={{
                table: ({ children, ...props }) => (
                  <ScrollArea>
                    <Table striped highlightOnHover withTableBorder {...props}>
                      {children}
                    </Table>
                  </ScrollArea>
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
              }}
            >
              {msg.content}
            </ReactMarkdown>
          ))}
          {postChatMutation.isPending && <Loader size="sm" />}
        </ScrollArea>

        <Box
          style={{
            position: "sticky",
            bottom: 0,
            width: "100%",
            backgroundColor: "white",
          }}
        >
          <Group gap="xs" grow>
            <TextInput
              value={input}
              onChange={(e) => setInput(e.currentTarget.value)}
              placeholder="What can you do?"
              onKeyDown={(e) => e.key === "Enter" && sendMessage()}
              rightSection={
                postChatMutation.isPending ? (
                  <Loader size="xs" />
                ) : (
                  <ActionIcon onClick={sendMessage} aria-label="Send">
                    <IconArrowRight />
                  </ActionIcon>
                )
              }
              rightSectionPointerEvents={"all"}
              disabled={postChatMutation.isPending}
            />
          </Group>
        </Box>
      </Stack>
    </Container>
  );
}
