import { ActionIcon, Box, Group, Loader, Textarea } from "@mantine/core";
import { IconArrowRight } from "@tabler/icons-react";

interface ChatInputProps {
  value: string;
  onChange: (value: string) => void;
  onSend: () => void;
  isLoading: boolean;
}

export function ChatInput({
  value,
  onChange,
  onSend,
  isLoading,
}: ChatInputProps) {
  return (
    <Box style={{ flexShrink: 0 }}>
      <Group gap="xs" grow>
        <Textarea
          value={value}
          onChange={(e) => onChange(e.currentTarget.value)}
          placeholder="What can you do?"
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              onSend();
            }
          }}
          rightSection={
            isLoading ? (
              <Loader size="xs" />
            ) : (
              <ActionIcon onClick={onSend} aria-label="Send">
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
  );
}
