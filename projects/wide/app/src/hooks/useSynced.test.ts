import { describe, expect, it, renderHook, vi } from "@lab/test-utils";
import { useSynced } from "./useSynced";

describe("useSynced", () => {
  it("has no sync time before data arrives", () => {
    const { result } = renderHook(() => useSynced(undefined));

    expect(result.current).toBeNull();
  });

  it("records when each new result arrives", () => {
    vi.useFakeTimers({ now: new Date(2026, 0, 1, 9, 0) });
    const { result, rerender } = renderHook(({ data }) => useSynced(data), {
      initialProps: { data: { page: 1 } as object | undefined },
    });
    const first = result.current;

    vi.setSystemTime(new Date(2026, 0, 1, 9, 5));
    rerender({ data: { page: 2 } });

    expect(first).toEqual(new Date(2026, 0, 1, 9, 0));
    expect(result.current).toEqual(new Date(2026, 0, 1, 9, 5));
  });

  it("keeps the last sync time while a refetch has no data", () => {
    vi.useFakeTimers({ now: new Date(2026, 0, 1, 9, 0) });
    const { result, rerender } = renderHook(({ data }) => useSynced(data), {
      initialProps: { data: { page: 1 } as object | undefined },
    });

    rerender({ data: undefined });

    expect(result.current).toEqual(new Date(2026, 0, 1, 9, 0));
  });
});
