import "@mantine/core/styles.css";
import "mantine-react-table/styles.css";
import { MantineProvider } from "@mantine/core";
import { theme } from "./theme";
import { Router } from "./Router";
import {
  ApolloClient,
  ApolloProvider,
  InMemoryCache,
  split,
  HttpLink,
} from "@apollo/client";
import { getMainDefinition } from "@apollo/client/utilities";
import { GraphQLWsLink } from "@apollo/client/link/subscriptions";
import { StatsigProvider } from "./providers/StatsigProvider";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ModalsProvider } from "@mantine/modals";

import { createClient } from "graphql-ws";

const httpLink = new HttpLink({
  uri: "/api/graphql",
});
const wsLink = new GraphQLWsLink(
  createClient({
    url: `ws://${window.location.host}/ws/graphql`,
  }),
);

const splitLink = split(
  ({ query }) => {
    const definition = getMainDefinition(query);
    return (
      definition.kind === "OperationDefinition" &&
      definition.operation === "subscription"
    );
  },
  wsLink,
  httpLink,
);

const client = new ApolloClient({
  link: splitLink,
  cache: new InMemoryCache(),
});

const queryClient = new QueryClient();

export default function App() {
  return (
    <ApolloProvider client={client}>
      <QueryClientProvider client={queryClient}>
        <StatsigProvider>
          <MantineProvider theme={theme}>
            <ModalsProvider>
              <Router />
            </ModalsProvider>
          </MantineProvider>
        </StatsigProvider>
      </QueryClientProvider>
    </ApolloProvider>
  );
}
