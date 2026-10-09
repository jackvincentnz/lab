import { mockMatchMedia } from "@lab/test-utils/setup";
import { afterEach, vi } from "vitest";
import type { PropsWithChildren } from "react";
import { resetStatsigMock, statsigClient } from "./statsig";

afterEach(() => {
  resetStatsigMock();
});

mockMatchMedia(true);

vi.mock("@statsig/react-bindings", () => {
  return {
    StatsigProvider: ({ children }: PropsWithChildren<{ client?: unknown }>) =>
      children,
    useClientAsyncInit: () => ({
      client: statsigClient,
    }),
    useStatsigClient: () => ({
      client: statsigClient,
    }),
  };
});
