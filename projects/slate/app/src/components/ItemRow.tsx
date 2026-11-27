import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import {
  ActionIcon,
  Checkbox,
  Group,
  List,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { IconGripVertical } from "@tabler/icons-react";
import type { Item, ItemStatus } from "../api";
import classes from "./ItemRow.module.css";

const STALE_DAYS = 5;

const DAY_MS = 24 * 60 * 60 * 1000;

interface ItemRowProps {
  item: Item;
  rank: number;
  disabled: boolean;
  open: boolean;
  onOpen(): void;
  onStatusChange(status: ItemStatus): void;
}

export function ItemRow({
  item,
  rank,
  disabled,
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
  } = useSortable({ id: item.id, disabled });
  const done = item.status === "done";
  const unchangedDays = Math.floor(
    (Date.now() - Date.parse(item.updatedAt)) / DAY_MS,
  );
  const stale = item.status === "in_progress" && unchangedDays >= STALE_DAYS;

  return (
    <List.Item
      ref={setNodeRef}
      style={{ transform: CSS.Translate.toString(transform), transition }}
    >
      <Group
        className={classes["row"]}
        mod={{ open }}
        wrap="nowrap"
        gap="sm"
        pl="xs"
      >
        <ActionIcon
          ref={setActivatorNodeRef}
          className={classes["control"]}
          variant="subtle"
          color="gray"
          aria-label={`Move ${item.name}`}
          data-disabled={disabled}
          style={{ cursor: "grab" }}
          {...attributes}
          {...listeners}
        >
          <IconGripVertical size={16} color="var(--mantine-color-gray-5)" />
        </ActionIcon>
        <Checkbox
          className={classes["control"]}
          aria-label={`Mark ${item.name} done`}
          checked={done}
          disabled={disabled}
          onChange={(event) =>
            onStatusChange(event.currentTarget.checked ? "done" : "todo")
          }
        />
        <UnstyledButton
          className={classes["open"]}
          aria-label={`Open ${item.name}`}
          aria-expanded={open}
          onClick={onOpen}
        >
          <Text span size="sm" c="dimmed" miw={28}>
            #{rank}
          </Text>
          <Text
            span
            flex={1}
            size="sm"
            fw={500}
            td={done ? "line-through" : undefined}
            c={done ? "dimmed" : undefined}
          >
            {item.name}
          </Text>
          {stale && (
            <Text span size="xs" fw={500} c="orange.8">
              No change for {unchangedDays} days
            </Text>
          )}
        </UnstyledButton>
      </Group>
    </List.Item>
  );
}
