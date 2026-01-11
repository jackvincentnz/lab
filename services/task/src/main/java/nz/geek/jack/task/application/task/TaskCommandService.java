package nz.geek.jack.task.application.task;

import nz.geek.jack.libs.ddd.es.persistence.AggregateStore;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskId;
import nz.geek.jack.task.domain.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskCommandService {

  private final TaskRepository taskRepository;

  private final AggregateStore aggregateStore;

  public TaskCommandService(TaskRepository taskRepository, AggregateStore aggregateStore) {
    this.taskRepository = taskRepository;
    this.aggregateStore = aggregateStore;
  }

  public TaskId addTask(String title) {
    var task = Task.addTask(title);

    //    taskRepository.save(task);
    aggregateStore.save(task);

    return task.getId();
  }

  public void markTaskCompleted(TaskId taskId) {
    //    var task = taskRepository.get(taskId);
    var task = aggregateStore.get(taskId, Task.class);

    task.markCompleted();

    //    taskRepository.save(task);
    aggregateStore.save(task);
  }
}
