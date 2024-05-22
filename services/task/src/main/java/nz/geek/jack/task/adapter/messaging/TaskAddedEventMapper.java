package nz.geek.jack.task.adapter.messaging;

import nz.geek.jack.task.api.TaskAddedEvent;

public class TaskAddedEventMapper
    implements EventMapper<nz.geek.jack.task.domain.TaskAddedEvent, TaskAddedEvent> {

  @Override
  public TaskAddedEvent map(nz.geek.jack.task.domain.TaskAddedEvent taskAddedEvent) {
    return TaskAddedEvent.newBuilder()
        .setTaskId(taskAddedEvent.getAggregateId().toString())
        .setTitle(taskAddedEvent.getTitle())
        .setCreatedAt(taskAddedEvent.getCreatedAt().toString())
        .build();
  }
}
