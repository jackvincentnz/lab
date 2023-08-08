package nz.geek.jack.task.infrastructure.messaging;

import nz.geek.jack.task.api.TaskCompletedEvent;

public class TaskCompletedEventMapper
    implements EventMapper<nz.geek.jack.task.domain.TaskCompletedEvent, TaskCompletedEvent> {

  @Override
  public TaskCompletedEvent map(nz.geek.jack.task.domain.TaskCompletedEvent taskCompletedEvent) {
    return TaskCompletedEvent.newBuilder()
        .setTaskId(taskCompletedEvent.getTaskId().toString())
        .build();
  }
}
