import { InMemoryCache } from "@apollo/client";
import {
  MockedProvider,
  type MockedProviderProps,
} from "@apollo/client/testing/react";
import { MantineProvider } from "@mantine/core";
import {
  render as testingLibraryRender,
  type RenderResult,
} from "@testing-library/react";
import type { PropsWithChildren, ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";

export interface Options {
  mockedProvider?: MockedProviderProps;
  route?: string;
}

export function render(ui: ReactNode, options?: Options): RenderResult {
  return testingLibraryRender(ui, {
    wrapper: ({ children }: PropsWithChildren) => (
      <MantineProvider>
        <RouterWrapper route={options?.route}>
          <ApolloWrapper mockedProvider={options?.mockedProvider}>
            {children}
          </ApolloWrapper>
        </RouterWrapper>
      </MantineProvider>
    ),
  });
}

function ApolloWrapper({
  children,
  mockedProvider,
}: PropsWithChildren<{ mockedProvider?: MockedProviderProps }>) {
  if (!mockedProvider) {
    return children;
  }

  return (
    <MockedProvider cache={new InMemoryCache()} {...mockedProvider}>
      {children}
    </MockedProvider>
  );
}

function RouterWrapper({
  children,
  route,
}: PropsWithChildren<{ route?: string }>) {
  if (!route) {
    return children;
  }

  return <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>;
}
