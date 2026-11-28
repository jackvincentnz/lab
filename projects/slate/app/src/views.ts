import type { ListItemsInput } from "./api";
import { everyStatus, statusStyles } from "./statuses";

export interface View {
  label: string;
  query: ListItemsInput;
  empty: string;
}

export const views = {
  today: {
    label: "Today",
    query: {},
    empty: "Nothing on your slate yet. Add the first item above.",
  },
  delegated: {
    label: statusStyles.delegated.label,
    query: { statuses: ["delegated"] },
    empty: "Nothing delegated.",
  },
  deferred: {
    label: statusStyles.deferred.label,
    query: { statuses: ["deferred"] },
    empty: "Nothing deferred.",
  },
  done: {
    label: statusStyles.done.label,
    query: { statuses: ["done"], doneTodayOnly: false, orderBy: "doneAt" },
    empty: "Nothing done yet.",
  },
  wontDo: {
    label: statusStyles.wont_do.label,
    query: { statuses: ["wont_do"] },
    empty: "Nothing marked won't do.",
  },
  deleted: {
    label: "Deleted",
    query: {
      deleted: true,
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
      orderBy: "status",
    },
    empty: `No items match “${search}”.`,
  };
}
