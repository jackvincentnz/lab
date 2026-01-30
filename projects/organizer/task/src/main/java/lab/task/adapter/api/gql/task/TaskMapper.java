package lab.task.adapter.api.gql.task;

import lab.task.adapter.gql.schema.types.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
  public Task map(lab.task.domain.Task task) {
    return new Task(task.getId().toString(), task.getTitle(), task.isCompleted());
  }
}
