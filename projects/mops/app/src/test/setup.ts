import "@testing-library/jest-dom/vitest";
import { vi } from "vitest";
import { PropsWithChildren } from "react";
import { statsigClient } from "./statsig";

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

const consoleError = console.error;
vi.spyOn(console, "error").mockImplementation((...args) => {
  const message = args.join(" ");

  if (message.includes("InMemoryCache") && message.includes("addTypename")) {
    return;
  }

  if (message.includes("cache.diff") && message.includes("canonizeResults")) {
    return;
  }

  consoleError(...args);
});

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
