import {
  Badge,
  Box,
  Button,
  Card,
  Code,
  Collapse,
  Group,
  Stack,
  Text,
  UnstyledButton,
} from "@mantine/core";
import {
  IconCheck,
  IconChevronDown,
  IconChevronRight,
  IconDatabaseEdit,
  IconX,
} from "@tabler/icons-react";
import { useDisclosure } from "@mantine/hooks";
import { ToolCallStatus as GToolCallStatus } from "../../__generated__/graphql";

export type ToolCallStatus = GToolCallStatus;

export interface ToolCallApprovalProps {
  toolCall: {
    name: string;
    arguments: string;
    status: ToolCallStatus;
  };
  onApprove: () => void;
  onReject: () => void;
}

export function ToolCallApproval({
  toolCall,
  onApprove,
  onReject,
}: ToolCallApprovalProps) {
  const [opened, { close, toggle }] = useDisclosure(
    toolCall.status === "PENDING_APPROVAL",
  );

  let parsedArgs: object | null = null;
  try {
    parsedArgs = JSON.parse(toolCall.arguments);
  } catch {
    // Keep as string if not valid JSON
  }

  const isPending = toolCall.status === "PENDING_APPROVAL";
  const isApproved = toolCall.status === "APPROVED";

  const onApproveClick = () => {
    onApprove();
    close();
  };

  const onRejectClick = () => {
    onReject();
    close();
  };

  if (!isPending) {
    return (
      <Card withBorder shadow="sm" p="xs" radius="md">
        <UnstyledButton onClick={toggle} style={{ width: "100%" }}>
          <Group justify="space-between">
            <Group gap="xs">
              {opened ? (
                <IconChevronDown size={16} />
              ) : (
                <IconChevronRight size={16} />
              )}
              <IconDatabaseEdit size={16} />
              <Text size="sm" fw={500}>
                {toolCall.name}
              </Text>
            </Group>
            <Badge
              color={isApproved ? "green" : "red"}
              variant="light"
              size="sm"
              leftSection={
                isApproved ? <IconCheck size={12} /> : <IconX size={12} />
              }
            >
              {isApproved ? "Approved" : "Rejected"}
            </Badge>
          </Group>
        </UnstyledButton>

        <Collapse in={opened}>
          <Stack gap="sm" mt="sm">
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
          </Stack>
        </Collapse>
      </Card>
    );
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
            onClick={onRejectClick}
          >
            Reject
          </Button>
          <Button
            size="xs"
            variant="filled"
            color="green"
            leftSection={<IconCheck size={14} />}
            onClick={onApproveClick}
          >
            Approve
          </Button>
        </Group>
      </Stack>
    </Card>
  );
}
