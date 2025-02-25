import "@mantine/core/styles.css";
import "mantine-react-table/styles.css";
import { MantineProvider } from "@mantine/core";
import { theme } from "./theme";
import { Router } from "./Router";
import { ApolloClient, ApolloProvider, InMemoryCache } from "@apollo/client";
import { StatsigProvider } from "./providers/StatsigProvider";

const client = new ApolloClient({
  uri: "/graphql",
  cache: new InMemoryCache(),
});

export default function App() {
  return (
    <ApolloProvider client={client}>
      <StatsigProvider>
        <MantineProvider theme={theme}>
          <Router />
        </MantineProvider>
      </StatsigProvider>
    </ApolloProvider>
  );
}
