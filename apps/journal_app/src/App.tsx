import { ApolloClient, InMemoryCache, ApolloProvider } from "@apollo/client";
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import { AppShell } from "@lab/bubbles"; // FIXME: type safety for the library
import { EntriesPage } from "./entries";

const client = new ApolloClient({
  uri: "/graphql",
  cache: new InMemoryCache(),
});

function App() {
  return (
    <ApolloProvider client={client}>
      <AppShell title="Journal">
        <EntriesPage />
      </AppShell>
    </ApolloProvider>
  );
}

export default App;
