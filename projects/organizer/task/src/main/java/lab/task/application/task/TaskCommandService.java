package lab.task.application.task;

import lab.task.domain.Task;
import lab.task.domain.TaskId;
import lab.task.domain.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskCommandService {

  private final TaskRepository taskRepository;

  public TaskCommandService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public TaskId addTask(String title) {
    var task = Task.addTask(title);

    taskRepository.save(task);

    return task.getId();
  }

  public void markTaskCompleted(TaskId taskId) {
    var task = taskRepository.get(taskId);

    task.markCompleted();

    taskRepository.save(task);
  }
}
