package nz.geek.jack.example.domain;

import java.util.Collection;

public interface TaskRepository {

  void saveTask(Task task);

  Collection<Task> getAllTasks();

  Task getTask(TaskId id);
}
