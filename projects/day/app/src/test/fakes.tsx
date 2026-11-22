import type { PropsWithChildren } from "react";
import { vi, type Mocked } from "@lab/test-utils";
import { DayApiContext, type DayApi, type Item } from "../api";

export function fakeDayApi(items: Item[] = []): Mocked<DayApi> {
  return {
    listItems: vi.fn<DayApi["listItems"]>().mockResolvedValue(items),
    addItem: vi.fn<DayApi["addItem"]>(),
    updateItem: vi.fn<DayApi["updateItem"]>(),
  };
}

export function withDayApi(api: DayApi) {
  return function DayApiWrapper({ children }: PropsWithChildren) {
    return <DayApiContext value={api}>{children}</DayApiContext>;
  };
}

let itemCount = 0;

export function item(fields: Partial<Item> = {}): Item {
  itemCount += 1;
  return {
    id: crypto.randomUUID(),
    name: `Item ${itemCount}`,
    status: "todo",
    rank: `a${itemCount}`,
    createdAt: "2026-11-22T09:00:00.000Z",
    updatedAt: "2026-11-22T09:00:00.000Z",
    ...fields,
  };
}
