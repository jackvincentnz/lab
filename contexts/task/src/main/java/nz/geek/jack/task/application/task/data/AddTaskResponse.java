package nz.geek.jack.task.application.task.data;

import nz.geek.jack.task.domain.TaskId;

public record AddTaskResponse(TaskId taskId) {

  public static AddTaskResponse of(TaskId taskId) {
    return new AddTaskResponse(taskId);
  }
}
