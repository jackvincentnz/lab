import { useMutation } from "@apollo/client";
import { AddTaskDocument, GetTasksDocument } from "../__generated__/graphql";

export function AddTask() {
  let input: HTMLInputElement | null;
  const [addTaskMutation, { loading, error }] = useMutation(AddTaskDocument, {
    refetchQueries: [GetTasksDocument],
  });

  if (loading) return <>Submitting...</>;
  if (error) return <>Submission error! ${error.message}</>;

  return (
    <div>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (input?.value) {
            addTaskMutation({ variables: { input: { title: input.value } } });
            input.value = "";
          }
        }}
      >
        <input
          ref={(node) => {
            input = node;
          }}
        />
        <button type="submit">Add Task</button>
      </form>
    </div>
  );
}

export default AddTask;
