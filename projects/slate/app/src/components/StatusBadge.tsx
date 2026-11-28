import { Badge } from "@mantine/core";
import type { ItemStatus } from "../api";
import { statusStyles } from "../statuses";

interface StatusBadgeProps {
  status: ItemStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const { label, color } = statusStyles[status];
  return (
    <Badge variant="light" color={color} radius="sm">
      {label}
    </Badge>
  );
}
