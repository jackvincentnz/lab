import { ApolloClient, HttpLink, InMemoryCache } from "@apollo/client";
import { ApolloProvider } from "@apollo/client/react";
import { MantineProvider } from "@mantine/core";
import { Shell } from "@lab/bubbles";
import Tasks from "./tasks";

const client = new ApolloClient({
  link: new HttpLink({ uri: "/graphql" }),
  cache: new InMemoryCache(),
});

export default function App() {
  return (
    <ApolloProvider client={client}>
      <MantineProvider>
        <Shell title="Tasks">
          <Tasks />
        </Shell>
      </MantineProvider>
    </ApolloProvider>
  );
}
