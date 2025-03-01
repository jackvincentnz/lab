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
} from "@mantine/core";
import ReactMarkdown from "react-markdown";
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
      return fetch("/chat", {
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
            <ReactMarkdown key={index}>{msg.content}</ReactMarkdown>
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
              placeholder="Type your message..."
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
