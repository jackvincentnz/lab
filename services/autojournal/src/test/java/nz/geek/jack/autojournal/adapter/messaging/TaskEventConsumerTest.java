package nz.geek.jack.autojournal.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import nz.geek.jack.autojournal.application.TaskEventHandler;
import nz.geek.jack.task.api.TaskCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskEventConsumerTest {

  @Mock TaskEventHandler taskEventHandler;

  @InjectMocks TaskEventConsumer taskEventConsumer;

  @Test
  void listen_handlesTaskCompletedEvents() {
    var taskCompletedEvent = TaskCompletedEvent.getDefaultInstance();

    taskEventConsumer.listen(taskCompletedEvent, "task");

    verify(taskEventHandler)
        .handle(any(nz.geek.jack.autojournal.application.TaskCompletedEvent.class));
  }

  @Test
  void listen_mapsTaskCompletedEvents() {
    var taskId = "taskId";
    var taskCompletedEvent = TaskCompletedEvent.newBuilder().setTaskId(taskId).build();
    var mappedCaptor =
        ArgumentCaptor.forClass(nz.geek.jack.autojournal.application.TaskCompletedEvent.class);

    taskEventConsumer.listen(taskCompletedEvent, "task");

    verify(taskEventHandler).handle(mappedCaptor.capture());
    assertThat(mappedCaptor.getValue().taskId()).isEqualTo(taskId);
  }
}
