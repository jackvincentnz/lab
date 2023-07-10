package nz.geek.jack.task.application.task;

import java.util.Collection;
import nz.geek.jack.task.application.task.data.TaskQuery;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskQueryService {

  private final TaskRepository taskRepository;

  public TaskQueryService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public Collection<Task> getAllTasks() {
    return taskRepository.getAllTasks();
  }

  public Task getTask(TaskQuery query) {
    return taskRepository.getTask(query.taskId());
  }
}
