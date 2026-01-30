package lab.task.application.task;

import java.util.Collection;
import lab.task.domain.Task;
import lab.task.domain.TaskId;
import lab.task.domain.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskQueryService {

  private final TaskRepository taskRepository;

  public TaskQueryService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public Collection<Task> getAllTasks() {
    return taskRepository.getAll();
  }

  public Task getTask(TaskId taskId) {
    return taskRepository.get(taskId);
  }
}
