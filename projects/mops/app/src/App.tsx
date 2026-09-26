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

// Vite's base is /mops/ in local dev so the app can be served through the gateway, and / in
// the production bundle. API and subscription paths follow it.
const base = import.meta.env.BASE_URL;

const httpLink = new HttpLink({
  uri: `${base}api/graphql`,
});
const wsLink = new GraphQLWsLink(
  createClient({
    url: `ws://${window.location.host}${base}ws/graphql`,
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
