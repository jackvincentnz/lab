import type { ItemStatus } from "./api";

interface StatusStyle {
  label: string;
  color: string;
}

export const statusStyles: Record<ItemStatus, StatusStyle> = {
  todo: { label: "To do", color: "gray" },
  in_progress: { label: "In progress", color: "blue" },
  done: { label: "Done", color: "green" },
  deferred: { label: "Deferred", color: "yellow" },
  wont_do: { label: "Won't do", color: "red" },
  delegated: { label: "Delegated", color: "violet" },
};

export const everyStatus = Object.keys(statusStyles) as ItemStatus[];
