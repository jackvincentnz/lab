package lab.task.adapter.persistence;

import lab.libs.ddd.domain.NotFoundException;
import lab.libs.ddd.persistence.InMemoryAggregateStore;
import lab.task.domain.Task;
import lab.task.domain.TaskId;
import lab.task.domain.TaskRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTaskRepository extends InMemoryAggregateStore<TaskId, Task>
    implements TaskRepository {

  // The shared store throws a plain RuntimeException, which callers cannot tell from a failure.
  @Override
  public Task get(TaskId id) {
    var task = aggregates.get(id);
    if (task == null) {
      throw new NotFoundException(id);
    }
    return task;
  }
}
