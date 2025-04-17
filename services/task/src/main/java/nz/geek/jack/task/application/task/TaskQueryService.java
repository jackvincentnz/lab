package nz.geek.jack.task.application.task;

import java.util.Collection;
import nz.geek.jack.libs.ddd.es.persistence.AggregateStore;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskId;
import org.springframework.stereotype.Service;

@Service
public class TaskQueryService {

  private final AggregateStore taskRepository;

  public TaskQueryService(AggregateStore taskRepository) {
    this.taskRepository = taskRepository;
  }

  public Collection<Task> getAllTasks() {
    throw new RuntimeException("Not implemented");
  }

  public Task getTask(TaskId taskId) {
    return taskRepository.get(taskId, Task.class);
  }
}
