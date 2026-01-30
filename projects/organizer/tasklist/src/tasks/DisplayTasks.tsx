import { useMutation, useQuery } from "@apollo/client";
import { List } from "@mantine/core";
import { IconCircle, IconCircleCheck } from "@tabler/icons-react";

import {
  GetTasksDocument,
  MarkTaskCompletedDocument,
} from "../__generated__/graphql";

export function DisplayTasks() {
  const { data } = useQuery(GetTasksDocument);

  const [markTaskCompletedMutation] = useMutation(MarkTaskCompletedDocument, {
    refetchQueries: [GetTasksDocument],
  });

  function handleClick(taskId: string) {
    markTaskCompletedMutation({ variables: { input: { id: taskId } } });
  }

  const tasks = data?.allTasks.map((task) => {
    return (
      <List.Item
        key={task.id}
        icon={task.isCompleted ? <IconCircleCheck /> : <IconCircle />}
        onClick={() => handleClick(task.id)}
      >
        {task.title}
      </List.Item>
    );
  });

  return (
    <List spacing="xs" size="sm" center mt="xs">
      {tasks}
    </List>
  );
}

export default DisplayTasks;
