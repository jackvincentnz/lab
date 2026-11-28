import { useQuery } from "@tanstack/react-query";
import { type Item, useTRPC } from "./api";
import { everyStatus } from "./statuses";
import type { ViewName } from "./views";

const ITEM_PARAMETER = "item";

export function itemLink(item: Item): string {
  const link = new URL("/", window.location.origin);
  link.searchParams.set(ITEM_PARAMETER, item.id);
  return link.href;
}

export function linkedItemId(): string | undefined {
  return (
    new URLSearchParams(window.location.search).get(ITEM_PARAMETER) ?? undefined
  );
}

export type LinkedItem =
  { found: true; view: ViewName; id: string } | { found: false };

export function useLinkedItem(id: string | undefined): LinkedItem | undefined {
  const trpc = useTRPC();
  const kept = useQuery({
    ...trpc.listItems.queryOptions({
      statuses: everyStatus,
      doneTodayOnly: false,
    }),
    enabled: id !== undefined,
  });
  const keptItem = kept.data?.find((item) => item.id === id);
  const deleted = useQuery({
    ...trpc.listItems.queryOptions({
      deleted: true,
      statuses: everyStatus,
      doneTodayOnly: false,
    }),
    enabled: id !== undefined && kept.isSuccess && keptItem === undefined,
  });
  const deletedItem = deleted.data?.find((item) => item.id === id);

  if (id === undefined) {
    return undefined;
  }
  if (keptItem !== undefined) {
    return { found: true, view: viewOf(keptItem), id };
  }
  if (deletedItem !== undefined) {
    return { found: true, view: "deleted", id };
  }
  if (kept.isError || deleted.isError || deleted.isSuccess) {
    return { found: false };
  }
  return undefined;
}

export function viewOf(item: Item): ViewName {
  switch (item.status) {
    case "todo":
    case "in_progress":
      return "today";
    case "wont_do":
      return "wontDo";
    default:
      return item.status;
  }
}
