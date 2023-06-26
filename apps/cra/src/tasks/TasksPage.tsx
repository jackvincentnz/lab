import { ApolloClient, InMemoryCache, ApolloProvider } from "@apollo/client";
import DisplayTasks from "./DisplayTasks";

const client = new ApolloClient({
  uri: "http://localhost:3000/graphql",
  cache: new InMemoryCache(),
});

export function TasksPage() {
  return (
    <ApolloProvider client={client}>
      <DisplayTasks />
    </ApolloProvider>
  );
}

export default TasksPage;
