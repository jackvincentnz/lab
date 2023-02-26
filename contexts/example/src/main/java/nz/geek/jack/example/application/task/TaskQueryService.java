package nz.geek.jack.example.application.task;

import java.util.Collection;
import nz.geek.jack.example.application.task.data.TaskQuery;
import nz.geek.jack.example.domain.Task;
import nz.geek.jack.example.domain.TaskRepository;
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
