import { ApolloClient, InMemoryCache, ApolloProvider } from "@apollo/client";
import { MantineProvider } from "@mantine/core";
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-expect-error
import { Shell } from "@lab/bubbles"; // FIXME: type safety for the library
import { EntriesPage } from "./entries";

import "@lab/bubbles/style.css";

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
