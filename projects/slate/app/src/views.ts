import type { ItemStatus, ListItemsInput } from "./api";

export interface View {
  label: string;
  query: ListItemsInput;
  empty: string;
}

// A record, so that the compiler asks for a new status here too.
const everyStatus = Object.keys({
  todo: true,
  in_progress: true,
  done: true,
  deferred: true,
  wont_do: true,
  delegated: true,
} satisfies Record<ItemStatus, true>) as ItemStatus[];

export const views = {
  today: {
    label: "Today",
    query: {},
    empty: "Nothing on your slate yet. Add the first item above.",
  },
  delegated: {
    label: "Delegated",
    query: { statuses: ["delegated"] },
    empty: "Nothing delegated.",
  },
  deferred: {
    label: "Deferred",
    query: { statuses: ["deferred"] },
    empty: "Nothing deferred.",
  },
  done: {
    label: "Done",
    query: { statuses: ["done"], doneTodayOnly: false, orderBy: "doneAt" },
    empty: "Nothing done yet.",
  },
  wontDo: {
    label: "Won't do",
    query: { statuses: ["wont_do"] },
    empty: "Nothing marked won't do.",
  },
  deleted: {
    label: "Deleted",
    query: {
      deleted: "only",
      statuses: everyStatus,
      doneTodayOnly: false,
      orderBy: "updatedAt",
    },
    empty: "Nothing deleted.",
  },
} satisfies Record<string, View>;

export type ViewName = keyof typeof views;

export function searchView(search: string): View {
  return {
    label: "Search results",
    query: {
      search,
      statuses: everyStatus,
      doneTodayOnly: false,
      deleted: "include",
      orderBy: "status",
    },
    empty: `No items match “${search}”.`,
  };
}
