import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";
import { resetStatsigMock } from "./statsig";

afterEach(() => {
  cleanup();
  resetStatsigMock();
});
