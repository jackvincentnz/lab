package nz.geek.jack.task.application.task;

import nz.geek.jack.task.application.task.data.AddTaskCommand;
import nz.geek.jack.task.application.task.data.AddTaskResponse;
import nz.geek.jack.task.application.task.data.MarkTaskCompletedCommand;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskCommandService {

  private final TaskRepository taskRepository;

  public TaskCommandService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public AddTaskResponse addTask(AddTaskCommand command) {
    var task = Task.addTask(command.title());

    taskRepository.saveTask(task);

    return AddTaskResponse.of(task.getId());
  }

  public void markTaskCompleted(MarkTaskCompletedCommand command) {
    var task = taskRepository.getTask(command.taskId());

    task.markCompleted();

    taskRepository.saveTask(task);
  }
}
