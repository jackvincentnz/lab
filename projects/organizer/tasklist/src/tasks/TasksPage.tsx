import { ApolloClient, HttpLink, InMemoryCache } from "@apollo/client";
import { ApolloProvider } from "@apollo/client/react";
import DisplayTasks from "./DisplayTasks";
import AddTask from "./AddTask";

const client = new ApolloClient({
  link: new HttpLink({ uri: "/graphql" }),
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
