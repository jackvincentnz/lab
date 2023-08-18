package nz.geek.jack.task.adapter.web.gql.task;

import nz.geek.jack.task.adapter.gql.schema.types.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
  public Task map(nz.geek.jack.task.domain.Task task) {
    return new Task(task.getId().toString(), task.getTitle(), task.isCompleted());
  }
}
