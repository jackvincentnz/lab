import "@mantine/core/styles.layer.css";
import "mantine-datatable/styles.layer.css";
import { MantineProvider } from "@mantine/core";
import { theme } from "./theme";
import { Router } from "./Router";
import { ApolloClient, InMemoryCache, split, HttpLink } from "@apollo/client";
import { ApolloProvider } from "@apollo/client/react";
import { getMainDefinition } from "@apollo/client/utilities";
import { GraphQLWsLink } from "@apollo/client/link/subscriptions";
import { StatsigProvider } from "./providers/StatsigProvider";
import { ModalsProvider } from "@mantine/modals";

import { createClient } from "graphql-ws";
import { csrfFetch } from "./csrfFetch";

const httpLink = new HttpLink({
  uri: "/api/graphql",
  fetch: csrfFetch,
});
const wsLink = new GraphQLWsLink(
  createClient({
    url: `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/ws/graphql`,
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

export default function App() {
  return (
    <ApolloProvider client={client}>
      <StatsigProvider>
        <MantineProvider theme={theme}>
          <ModalsProvider>
            <Router />
          </ModalsProvider>
        </MantineProvider>
      </StatsigProvider>
    </ApolloProvider>
  );
}
