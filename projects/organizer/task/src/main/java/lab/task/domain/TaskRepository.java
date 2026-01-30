package lab.task.domain;

import java.util.Collection;

public interface TaskRepository {

  void save(Task task);

  Collection<Task> getAll();

  Task get(TaskId id);
}
