import { Stack, Text, Title } from "@mantine/core";
import type { ItemChange } from "../api";
import { dayName } from "../dates";
import { statusStyles } from "../statuses";

interface ItemChangesProps {
  changes: ItemChange[];
  now?: Date;
}

const actorNames: Record<ItemChange["actor"], string> = {
  user: "You",
  agent: "An agent",
};

function what({ kind, status }: ItemChange): string {
  switch (kind) {
    case "added":
      return "added it";
    case "name":
      return "renamed it";
    case "description":
      return "changed the description";
    case "sources":
      return "changed the sources";
    case "status":
      return `marked it ${status ? statusStyles[status].label.toLowerCase() : ""}`;
    case "rank":
      return "moved it";
    case "deleted":
      return "deleted it";
    case "restored":
      return "restored it";
  }
}

function when(at: Date, now: Date): string {
  const day = dayName(at, now);
  const time = at.toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  });
  return day === "Today" ? time : `${day} ${time}`;
}

export function ItemChanges({ changes, now = new Date() }: ItemChangesProps) {
  if (changes.length === 0) {
    return null;
  }

  return (
    <Stack gap="xs">
      <Title order={3} size="xs" c="dimmed" tt="uppercase">
        History
      </Title>
      {changes.map((change, index) => (
        <Text key={index} size="sm">
          {actorNames[change.actor]} {what(change)}{" "}
          <Text span size="sm" c="dimmed">
            · {when(new Date(change.at), now)}
          </Text>
        </Text>
      ))}
    </Stack>
  );
}
