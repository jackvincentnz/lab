import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach, vi } from "vitest";

afterEach(cleanup);
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
const { getComputedStyle } = window;
window.getComputedStyle = (element) => getComputedStyle(element);
class ResizeObserver {
  observe() {
    /* jsdom has no layout to observe. */
  }
  unobserve() {
    /* jsdom has no layout to observe. */
  }
  disconnect() {
    /* No browser observer is created in tests. */
  }
}
window.ResizeObserver = ResizeObserver;
