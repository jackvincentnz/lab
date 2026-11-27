import { Button, Group, Notification, Text } from "@mantine/core";
import { IconArrowBackUp } from "@tabler/icons-react";
import type { Item } from "../api";

interface DeletedItemNoticeProps {
  item: Item;
  disabled: boolean;
  onUndo(): void;
  onDismiss(): void;
}

export function DeletedItemNotice({
  item,
  disabled,
  onUndo,
  onDismiss,
}: DeletedItemNoticeProps) {
  return (
    <Notification
      color="gray"
      withBorder
      onClose={onDismiss}
      closeButtonProps={{ "aria-label": "Dismiss" }}
    >
      <Group justify="space-between" wrap="nowrap">
        <Text size="sm">Deleted {item.name}</Text>
        <Button
          variant="light"
          size="xs"
          leftSection={<IconArrowBackUp size={14} />}
          disabled={disabled}
          onClick={onUndo}
          autoFocus
        >
          Undo
        </Button>
      </Group>
    </Notification>
  );
}
