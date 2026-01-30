package lab.task.adapter.messaging;

import lab.task.api.TaskCompletedEvent;

public class TaskCompletedEventMapper
    implements EventMapper<lab.task.domain.TaskCompletedEvent, TaskCompletedEvent> {

  @Override
  public TaskCompletedEvent map(lab.task.domain.TaskCompletedEvent taskCompletedEvent) {
    return TaskCompletedEvent.newBuilder()
        .setTaskId(taskCompletedEvent.getAggregateId().toString())
        .build();
  }
}
