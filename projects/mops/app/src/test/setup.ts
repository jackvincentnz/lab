import "@testing-library/jest-dom/vitest";
import { vi } from "vitest";

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
    useStatsigClient: () => ({
      client: {
        logEvent: () => {
          // intentionally empty
        },
      },
    }),
  };
});
