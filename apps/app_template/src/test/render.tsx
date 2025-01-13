import {
  RenderResult,
  render as testingLibraryRender,
} from "@testing-library/react";
import { MantineProvider } from "@mantine/core";

export function render(ui: React.ReactNode): RenderResult {
  return testingLibraryRender(ui, {
    wrapper: ({ children }: { children: React.ReactNode }) => (
      <MantineProvider>{children}</MantineProvider>
    ),
  });
}
