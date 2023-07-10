import { useQuery } from "@apollo/client";
import { List } from "@mantine/core";
import { IconCircle } from "@tabler/icons-react";

import { GetTasksDocument } from "../__generated__/graphql";

export function DisplayTasks() {
  const { data } = useQuery(GetTasksDocument);

  const tasks = data?.allTasks.map((task) => {
    return <List.Item key={task.id}>{task.title}</List.Item>;
  });

  return (
    <List spacing="xs" size="sm" center icon={<IconCircle size="1rem" />}>
      {tasks}
    </List>
  );
}

export default DisplayTasks;
