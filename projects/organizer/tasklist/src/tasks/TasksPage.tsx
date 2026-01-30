import { ApolloClient, InMemoryCache, ApolloProvider } from "@apollo/client";
import DisplayTasks from "./DisplayTasks";
import AddTask from "./AddTask";

const client = new ApolloClient({
  uri: "/graphql",
  cache: new InMemoryCache(),
});

export function TasksPage() {
  return (
    <ApolloProvider client={client}>
      <AddTask />
      <DisplayTasks />
    </ApolloProvider>
  );
}

export default TasksPage;
