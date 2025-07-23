import "@mantine/core/styles.css";
import "mantine-react-table/styles.css";
import { MantineProvider } from "@mantine/core";
import { theme } from "./theme";
import { Router } from "./Router";
import { ApolloClient, ApolloProvider, InMemoryCache } from "@apollo/client";
import { StatsigProvider } from "./providers/StatsigProvider";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ModalsProvider } from "@mantine/modals";

const client = new ApolloClient({
  uri: "/api/graphql",
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
