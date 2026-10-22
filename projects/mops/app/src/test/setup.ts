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
export const ciNonce = "exp/fe-scale-only-x5-exclusive-1-1792629585";
