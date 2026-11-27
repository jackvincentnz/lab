import type { ReactNode } from "react";
import { List, Stack } from "@mantine/core";
import type { Item, ItemStatus } from "../api";
import { dayName } from "../dates";
import { DeletedItemRow, ItemRow } from "./ItemRow";
import { ListHeading } from "./ListHeading";

interface ViewListProps {
  label: string;
  items: Item[];
  groupByDayDone: boolean;
  disabled: boolean;
  openId: string | undefined;
  onOpen(item: Item): void;
  onStatusChange(item: Item, status: ItemStatus): void;
  onRestore?(item: Item): void;
}

export function ViewList({
  label,
  items,
  groupByDayDone,
  disabled,
  openId,
  onOpen,
  onStatusChange,
  onRestore,
}: ViewListProps) {
  function rows(group: Item[]) {
    return group.map((item) =>
      onRestore === undefined ? (
        <ItemRow
          key={item.id}
          item={item}
          disabled={disabled}
          open={item.id === openId}
          onOpen={() => onOpen(item)}
          onStatusChange={(status) => onStatusChange(item, status)}
        />
      ) : (
        <DeletedItemRow
          key={item.id}
          item={item}
          disabled={disabled}
          onRestore={() => onRestore(item)}
        />
      ),
    );
  }

  if (!groupByDayDone) {
    return <ItemRows label={label}>{rows(items)}</ItemRows>;
  }

  return (
    <Stack gap="lg">
      {byDayDone(items).map(({ day, items: done }) => (
        <Stack key={day} gap="xs">
          <ListHeading>{day}</ListHeading>
          <ItemRows label={day}>{rows(done)}</ItemRows>
        </Stack>
      ))}
    </Stack>
  );
}

interface ItemRowsProps {
  label: string;
  children: ReactNode[];
}

function ItemRows({ label, children }: ItemRowsProps) {
  return (
    <List
      listStyleType="none"
      spacing={6}
      aria-label={label}
      styles={{ itemWrapper: { display: "block" } }}
    >
      {children}
    </List>
  );
}

interface DayDone {
  day: string;
  items: Item[];
}

function byDayDone(items: Item[]): DayDone[] {
  const now = new Date();
  const days: DayDone[] = [];
  for (const item of items) {
    const day = dayName(new Date(item.doneAt!), now);
    const last = days.at(-1);
    if (last?.day === day) {
      last.items.push(item);
    } else {
      days.push({ day, items: [item] });
    }
  }
  return days;
}
