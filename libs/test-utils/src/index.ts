// Carry the DOM matcher types that setup.ts registers to every test that imports this module.
/// <reference types="@testing-library/jest-dom/vitest" preserve="true" />
export * from "vitest";
export {
  act,
  cleanup,
  fireEvent,
  renderHook,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
export { default as userEvent } from "@testing-library/user-event";
export { render, type Options } from "./render.js";
