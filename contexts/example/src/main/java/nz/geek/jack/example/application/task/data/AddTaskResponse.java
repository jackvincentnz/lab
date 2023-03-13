package nz.geek.jack.example.application.task.data;

import nz.geek.jack.example.domain.TaskId;

public record AddTaskResponse(TaskId taskId) {

  public static AddTaskResponse of(TaskId taskId) {
    return new AddTaskResponse(taskId);
  }
}
