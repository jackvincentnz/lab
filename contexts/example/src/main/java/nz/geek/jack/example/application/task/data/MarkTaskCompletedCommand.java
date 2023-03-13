package nz.geek.jack.example.application.task.data;

import nz.geek.jack.example.domain.TaskId;

public interface MarkTaskCompletedCommand {

  TaskId taskId();
}
