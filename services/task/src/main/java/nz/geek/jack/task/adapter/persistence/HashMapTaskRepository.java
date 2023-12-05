package nz.geek.jack.task.adapter.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskId;
import nz.geek.jack.task.domain.TaskRepository;
import org.springframework.stereotype.Repository;

@Repository
public class HashMapTaskRepository implements TaskRepository {

  private final Map<TaskId, Task> tasks = new ConcurrentHashMap<>();

  @Override
  public void saveTask(Task task) {
    tasks.put(task.getId(), task);
  }

  @Override
  public Collection<Task> getAllTasks() {
    return tasks.values();
  }

  @Override
  public Task getTask(TaskId id) {
    if (tasks.containsKey(id)) {
      return tasks.get(id);
    }
    throw new RuntimeException(String.format("Task: %s does not exist", id));
  }
}
