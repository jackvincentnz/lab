import {
  RenderResult,
  render as testingLibraryRender,
} from "@testing-library/react";
import { MantineProvider } from "@mantine/core";
import { MockedProvider } from "@apollo/client/testing";
import { MockedProviderProps } from "@apollo/client/testing/react/MockedProvider";

export interface Options {
  mockedProvider?: MockedProviderProps;
}

export function render(ui: React.ReactNode, options?: Options): RenderResult {
  return testingLibraryRender(ui, {
    wrapper: ({ children }: { children: React.ReactNode }) => (
      <MockedProvider {...options?.mockedProvider}>
        <MantineProvider>{children}</MantineProvider>
      </MockedProvider>
    ),
  });
}
