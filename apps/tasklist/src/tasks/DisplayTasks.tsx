import { useQuery } from "@apollo/client";
import { GetTasksDocument } from "../__generated__/graphql";

export function DisplayTasks() {
  const { data } = useQuery(GetTasksDocument);

  const tasks = data?.allTasks.map((task) => {
    return <li key={task.id}>{task.title}</li>;
  });

  return <ul>{tasks}</ul>;
}

export default DisplayTasks;
