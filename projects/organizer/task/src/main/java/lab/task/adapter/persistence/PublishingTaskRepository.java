package lab.task.adapter.persistence;

import java.util.Collection;
import lab.task.adapter.messaging.DomainEventPublisher;
import lab.task.domain.Task;
import lab.task.domain.TaskId;
import lab.task.domain.TaskRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class PublishingTaskRepository implements TaskRepository {

  private final TaskRepository taskRepository;

  private final DomainEventPublisher domainEventPublisher;

  public PublishingTaskRepository(
      TaskRepository taskRepository, DomainEventPublisher domainEventPublisher) {
    this.taskRepository = taskRepository;
    this.domainEventPublisher = domainEventPublisher;
  }

  @Override
  public void save(Task task) {
    domainEventPublisher.publish(task.flushEvents());
    taskRepository.save(task);
  }

  @Override
  public Collection<Task> getAll() {
    return taskRepository.getAll();
  }

  @Override
  public Task get(TaskId id) {
    return taskRepository.get(id);
  }
}
