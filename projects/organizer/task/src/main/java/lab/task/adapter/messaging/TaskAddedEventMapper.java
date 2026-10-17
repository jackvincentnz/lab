package lab.task.adapter.messaging;

import lab.task.api.TaskAddedEvent;

public class TaskAddedEventMapper
    implements EventMapper<lab.task.domain.TaskAddedEvent, TaskAddedEvent> {

  @Override
  public TaskAddedEvent map(lab.task.domain.TaskAddedEvent taskAddedEvent) {
    return TaskAddedEvent.newBuilder()
        .setTaskId(taskAddedEvent.getAggregateId().toString())
        .setTitle(taskAddedEvent.getTitle())
        .setCreatedAt(taskAddedEvent.getCreatedAt().toString())
        .build();
  }
}
