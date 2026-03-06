import {
  RenderResult,
  render as testingLibraryRender,
} from "@testing-library/react";
import { InMemoryCache } from "@apollo/client";
import { MantineProvider } from "@mantine/core";
import { MockedProvider } from "@apollo/client/testing";
import { MockedProviderProps } from "@apollo/client/testing/react/MockedProvider";
import { ModalsProvider } from "@mantine/modals";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { StatsigProvider } from "@statsig/react-bindings";
import { PropsWithChildren } from "react";
import { statsigClient } from "./statsig";

export interface Options {
  mockedProvider?: MockedProviderProps;
  route?: string;
  path?: string;
  queryClient?: QueryClient;
}

export function render(ui: React.ReactNode, options?: Options): RenderResult {
  const queryClient =
    options?.queryClient ??
    new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

  return testingLibraryRender(ui, {
    wrapper: ({ children }: PropsWithChildren) => (
      <QueryClientProvider client={queryClient}>
        <StatsigProvider client={statsigClient as never}>
          <MantineProvider>
            <ModalsProvider>
              <RouterWrapper route={options?.route} path={options?.path}>
                <ApolloWrapper mockedProvider={options?.mockedProvider}>
                  {children}
                </ApolloWrapper>
              </RouterWrapper>
            </ModalsProvider>
          </MantineProvider>
        </StatsigProvider>
      </QueryClientProvider>
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
