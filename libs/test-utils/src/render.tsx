import {
  render as testingLibraryRender,
  type RenderResult,
  type RenderOptions,
} from "@testing-library/react";
import { InMemoryCache } from "@apollo/client";
import { MantineProvider } from "@mantine/core";
import {
  MockedProvider,
  type MockedProviderProps,
} from "@apollo/client/testing/react";
import { ModalsProvider } from "@mantine/modals";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import type { PropsWithChildren } from "react";

export interface Options extends Omit<RenderOptions, "wrapper"> {
  wrapper?: React.ComponentType<PropsWithChildren>;
  mockedProvider?: MockedProviderProps;
  route?: string;
  path?: string;
}

export function render(
  ui: React.ReactNode,
  options: Options = {},
): RenderResult {
  const {
    wrapper: Wrapper = ReactFragment,
    route,
    path,
    mockedProvider,
    ...renderOptions
  } = options;
  return testingLibraryRender(ui, {
    ...renderOptions,
    wrapper: ({ children }: PropsWithChildren) => (
      <Wrapper>
        <MantineProvider>
          <ModalsProvider>
            <RouterWrapper route={route} path={path}>
              <ApolloWrapper mockedProvider={mockedProvider}>
                {children}
              </ApolloWrapper>
            </RouterWrapper>
          </ModalsProvider>
        </MantineProvider>
      </Wrapper>
    ),
  });
}

function ReactFragment({ children }: PropsWithChildren) {
  return children;
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
  path,
}: PropsWithChildren<{ route?: string; path?: string }>) {
  if (!route) {
    return children;
  }

  if (!path) {
    return <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>;
  }

  return (
    <MemoryRouter initialEntries={[route]}>
      <Routes>
        <Route path={path} element={<>{children}</>} />
      </Routes>
    </MemoryRouter>
  );
}
