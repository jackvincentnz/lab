import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach, vi } from "vitest";
import type { PropsWithChildren } from "react";
import { resetStatsigMock, statsigClient } from "./statsig";

afterEach(() => {
  cleanup();
  resetStatsigMock();
});

const { getComputedStyle } = window;
window.getComputedStyle = (elt) => getComputedStyle(elt);
window.HTMLElement.prototype.scrollIntoView = () => {
  // intentionally empty
};

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: vi.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

class ResizeObserver {
  observe() {
    // intentionally empty
  }
  unobserve() {
    // intentionally empty
  }
  disconnect() {
    // intentionally empty
  }
}

window.ResizeObserver = ResizeObserver;

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
