import { ApolloClient, InMemoryCache, ApolloProvider } from "@apollo/client";
import { MantineProvider } from "@mantine/core";
import { Shell } from "@lab/bubbles";
import { EntriesPage } from "./entries";

const client = new ApolloClient({
  uri: "/graphql",
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
