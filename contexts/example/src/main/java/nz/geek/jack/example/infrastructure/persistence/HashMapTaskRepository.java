package nz.geek.jack.example.infrastructure.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nz.geek.jack.example.domain.Task;
import nz.geek.jack.example.domain.TaskId;
import nz.geek.jack.example.domain.TaskRepository;
import nz.geek.jack.example.infrastructure.exception.NotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class HashMapTaskRepository implements TaskRepository {

  private static final Map<TaskId, Task> TASKS = new ConcurrentHashMap<>();

  @Override
  public void saveTask(Task task) {
    TASKS.put(task.getId(), task);
  }

  @Override
  public Collection<Task> getAllTasks() {
    return TASKS.values();
  }

  @Override
  public Task getTask(TaskId id) {
    if (TASKS.containsKey(id)) {
      return TASKS.get(id);
    }
    throw new NotFoundException(String.format("Task: %s does not exist", id));
  }
}
