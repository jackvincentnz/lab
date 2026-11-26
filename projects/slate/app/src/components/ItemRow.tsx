import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import {
  ActionIcon,
  Badge,
  Checkbox,
  Group,
  List,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { IconGripVertical } from "@tabler/icons-react";
import type { Item, ItemStatus } from "../api";

interface ItemRowProps {
  item: Item;
  saving: boolean;
  open: boolean;
  onOpen(): void;
  onStatusChange(status: ItemStatus): void;
}

export function ItemRow({
  item,
  saving,
  open,
  onOpen,
  onStatusChange,
}: ItemRowProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
  } = useSortable({ id: item.id, disabled: saving });
  const done = item.status === "done";

  return (
    <List.Item
      ref={setNodeRef}
      style={{ transform: CSS.Translate.toString(transform), transition }}
    >
      <Group
        wrap="nowrap"
        gap="sm"
        px="xs"
        py={6}
        bg={open ? "var(--mantine-primary-color-light)" : undefined}
        style={{ borderRadius: "var(--mantine-radius-sm)" }}
      >
        <ActionIcon
          ref={setActivatorNodeRef}
          variant="subtle"
          color="gray"
          aria-label={`Move ${item.name}`}
          data-disabled={saving}
          style={{ cursor: "grab" }}
          {...attributes}
          {...listeners}
        >
          <IconGripVertical size={16} />
        </ActionIcon>
        <Checkbox
          aria-label={`Mark ${item.name} done`}
          checked={done}
          disabled={saving}
          onChange={(event) =>
            onStatusChange(event.currentTarget.checked ? "done" : "todo")
          }
        />
        <UnstyledButton
          flex={1}
          aria-label={`Open ${item.name}`}
          aria-expanded={open}
          onClick={onOpen}
        >
          <Text
            span
            td={done ? "line-through" : undefined}
            c={done ? "dimmed" : undefined}
          >
            {item.name}
          </Text>
        </UnstyledButton>
        {item.status === "doing" && <Badge variant="light">Doing</Badge>}
      </Group>
    </List.Item>
  );
}
