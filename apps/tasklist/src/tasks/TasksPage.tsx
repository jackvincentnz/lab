import { ApolloClient, InMemoryCache, ApolloProvider } from "@apollo/client";
import DisplayTasks from "./DisplayTasks";
import AddTask from "./AddTask";
import { Container } from "@mantine/core";

const client = new ApolloClient({
  uri: "http://localhost:3001/graphql",
  cache: new InMemoryCache(),
});

export function TasksPage() {
  return (
    <ApolloProvider client={client}>
      <Container>
        <AddTask />
      </Container>

      <Container mt="xs">
        <DisplayTasks />
      </Container>
    </ApolloProvider>
  );
}

export default TasksPage;
