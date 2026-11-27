import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import {
  ActionIcon,
  Badge,
  Button,
  Checkbox,
  Group,
  List,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { IconGripVertical } from "@tabler/icons-react";
import type { CSSProperties, ReactNode, Ref } from "react";
import type { Item, ItemStatus } from "../api";
import { daysSince } from "../dates";
import classes from "./ItemRow.module.css";
import { StatusBadge } from "./StatusBadge";

const STALE_DAYS = 5;

interface ItemRowProps {
  item: Item;
  rank?: number;
  showStatus?: boolean;
  disabled: boolean;
  open: boolean;
  onOpen(): void;
  onStatusChange(status: ItemStatus): void;
}

interface SortableItemRowProps extends ItemRowProps {
  rank: number;
}

interface RowLayoutProps extends ItemRowProps {
  handle?: ReactNode;
  rowRef?: Ref<HTMLLIElement>;
  style?: CSSProperties;
}

export function SortableItemRow(props: SortableItemRowProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: props.item.id, disabled: props.disabled });

  return (
    <ItemRow
      {...props}
      rowRef={setNodeRef}
      style={{
        transform: CSS.Translate.toString(transform),
        transition,
        position: "relative",
        // The rows a drag shifts paint in list order, so a later row would hide the dragged one.
        zIndex: isDragging ? 1 : undefined,
      }}
      handle={
        <ActionIcon
          ref={setActivatorNodeRef}
          className={classes["control"]}
          variant="subtle"
          color="gray"
          aria-label={`Move ${props.item.name}`}
          data-disabled={props.disabled}
          style={{ cursor: "grab" }}
          {...attributes}
          {...listeners}
        >
          <IconGripVertical size={16} color="var(--mantine-color-gray-5)" />
        </ActionIcon>
      }
    />
  );
}

export function ItemRow({
  item,
  rank,
  showStatus = false,
  disabled,
  open,
  onOpen,
  onStatusChange,
  handle,
  rowRef,
  style,
}: RowLayoutProps) {
  const done = item.status === "done";
  const unchangedDays = daysSince(new Date(item.updatedAt), new Date());
  const stale = item.status === "in_progress" && unchangedDays >= STALE_DAYS;

  return (
    <List.Item ref={rowRef} style={style}>
      <Group
        className={classes["row"]}
        mod={{ open }}
        wrap="nowrap"
        gap="sm"
        pl="xs"
      >
        {handle}
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
          {rank !== undefined && (
            <Text span size="sm" c="dimmed" miw={28}>
              #{rank}
            </Text>
          )}
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
          {showStatus && <StatusBadge status={item.status} />}
        </UnstyledButton>
      </Group>
    </List.Item>
  );
}

interface DeletedItemRowProps {
  item: Item;
  showStatus?: boolean;
  disabled: boolean;
  onRestore(): void;
}

export function DeletedItemRow({
  item,
  showStatus = false,
  disabled,
  onRestore,
}: DeletedItemRowProps) {
  return (
    <List.Item>
      <Group className={classes["row"]} wrap="nowrap" gap="sm" px="sm">
        <Text span flex={1} size="sm" fw={500} className={classes["name"]}>
          {item.name}
        </Text>
        {showStatus && <StatusBadge status={item.status} />}
        {showStatus && (
          <Badge variant="outline" color="gray" radius="sm">
            Deleted
          </Badge>
        )}
        <Button
          variant="default"
          size="compact-sm"
          aria-label={`Restore ${item.name}`}
          disabled={disabled}
          onClick={onRestore}
        >
          Restore
        </Button>
      </Group>
    </List.Item>
  );
}
