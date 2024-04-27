package nz.geek.jack.task.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import nz.geek.jack.libs.ddd.domain.DomainEvent;
import nz.geek.jack.task.adapter.messaging.DomainEventPublisher;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublishingTaskRepositoryTest {

  @Mock TaskRepository taskRepository;

  @Mock DomainEventPublisher domainEventPublisher;

  @Captor ArgumentCaptor<List<DomainEvent>> eventsCaptor;

  @InjectMocks PublishingTaskRepository publishingTaskRepository;

  @Test
  void saveTask_publishesPendingEvents() {
    var task = Task.addTask("my task");
    task.markCompleted();

    publishingTaskRepository.saveTask(task);

    verify(domainEventPublisher).publish(eventsCaptor.capture());
    var publishedEvents = eventsCaptor.getValue();

    assertThat(publishedEvents).hasSize(2);
  }
}
