package nz.geek.jack.task.application.task.data;

import nz.geek.jack.task.domain.TaskId;

public interface MarkTaskCompletedCommand {

  TaskId taskId();
}
