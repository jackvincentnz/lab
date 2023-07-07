import { useQuery } from "@apollo/client";
import { GetTasksDocument } from "../__generated__/graphql";

export function DisplayTasks() {
  const { data } = useQuery(GetTasksDocument);

  return <div>{data?.allTasks.length}</div>;
}

export default DisplayTasks;
