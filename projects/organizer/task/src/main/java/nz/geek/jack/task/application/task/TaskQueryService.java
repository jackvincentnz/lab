package nz.geek.jack.task.application.task;

import java.util.Collection;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskId;
import nz.geek.jack.task.domain.TaskRepository;
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
