import { Box, Button, Card, Code, Group, Stack, Text } from "@mantine/core";
import { IconCheck, IconDatabaseEdit, IconX } from "@tabler/icons-react";

interface ToolCallApprovalProps {
  toolCall: {
    name: string;
    arguments: string;
  };
  onApprove: () => void;
  onReject: () => void;
}

export function ToolCallApproval({
  toolCall,
  onApprove,
  onReject,
}: ToolCallApprovalProps) {
  let parsedArgs: object | null = null;
  try {
    parsedArgs = JSON.parse(toolCall.arguments);
  } catch {
    // Keep as string if not valid JSON
  }

  return (
    <Card withBorder shadow="sm" p="md" radius="md">
      <Stack gap="sm">
        <Group gap="xs">
          <IconDatabaseEdit size={18} />
          <Text fw={600} size="sm">
            Proposed Changes
          </Text>
        </Group>

        <Box>
          <Text size="xs" c="dimmed" mb={4}>
            Action
          </Text>
          <Code block>{toolCall.name}</Code>
        </Box>

        <Box>
          <Text size="xs" c="dimmed" mb={4}>
            Details
          </Text>
          <Code
            block
            style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}
          >
            {parsedArgs
              ? JSON.stringify(parsedArgs, null, 2)
              : toolCall.arguments}
          </Code>
        </Box>

        <Group justify="flex-end" gap="xs">
          <Button
            size="xs"
            variant="outline"
            color="red"
            leftSection={<IconX size={14} />}
            onClick={onReject}
          >
            Reject
          </Button>
          <Button
            size="xs"
            variant="filled"
            color="green"
            leftSection={<IconCheck size={14} />}
            onClick={onApprove}
          >
            Approve
          </Button>
        </Group>
      </Stack>
    </Card>
  );
}
