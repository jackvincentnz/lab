import { ApolloClient, HttpLink, InMemoryCache } from "@apollo/client";
import { ApolloProvider } from "@apollo/client/react";
import { MantineProvider } from "@mantine/core";
import { Shell } from "@lab/bubbles";
import { EntriesPage } from "./entries";

const client = new ApolloClient({
  link: new HttpLink({ uri: "/graphql" }),
  cache: new InMemoryCache(),
});

function App() {
  return (
    <ApolloProvider client={client}>
      <MantineProvider>
        <Shell title="Journal">
          <EntriesPage />
        </Shell>
      </MantineProvider>
    </ApolloProvider>
  );
}

export default App;
