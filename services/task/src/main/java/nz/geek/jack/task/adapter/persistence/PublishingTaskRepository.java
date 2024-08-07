package nz.geek.jack.task.adapter.persistence;

import java.util.Collection;
import nz.geek.jack.task.adapter.messaging.DomainEventPublisher;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskId;
import nz.geek.jack.task.domain.TaskRepository;
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
