import { useMutation } from "@apollo/client";
import { Button, Group, TextInput } from "@mantine/core";
import { useRef } from "react";
import { AddTaskDocument, GetTasksDocument } from "../__generated__/graphql";

export function AddTask() {
  const input = useRef<HTMLInputElement>(null);

  const [addTaskMutation, { error }] = useMutation(AddTaskDocument, {
    refetchQueries: [GetTasksDocument],
  });

  return (
    <>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (input.current?.value) {
            addTaskMutation({
              variables: { input: { title: input.current.value } },
            });
            input.current.value = "";
          }
        }}
      >
        <Group spacing="xs">
          <TextInput
            placeholder="Task name"
            aria-label="Task name"
            ref={input}
            error={error ? "Submission error!" : false}
            data-test="add-task-input"
          />
          <Button type="submit" data-test="add-task-button">
            Add task
          </Button>
        </Group>
      </form>
    </>
  );
}

export default AddTask;
