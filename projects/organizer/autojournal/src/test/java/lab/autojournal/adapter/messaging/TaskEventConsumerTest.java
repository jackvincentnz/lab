package lab.autojournal.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.google.protobuf.StringValue;
import java.util.UUID;
import lab.autojournal.application.TaskEventHandler;
import lab.task.api.TaskAddedEvent;
import lab.task.api.TaskCompletedEvent;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskEventConsumerTest extends TestBase {

  @Mock TaskEventHandler taskEventHandler;

  @InjectMocks TaskEventConsumer taskEventConsumer;

  @Test
  void on_handlesTaskCompletedEvents() {
    var taskCompletedEvent = TaskCompletedEvent.getDefaultInstance();

    taskEventConsumer.on(taskCompletedEvent);

    verify(taskEventHandler).handle(any(lab.autojournal.application.TaskCompletedEvent.class));
  }

  @Test
  void on_mapsTaskCompletedEvents() {
    var taskId = "taskId";
    var taskCompletedEvent = TaskCompletedEvent.newBuilder().setTaskId(taskId).build();
    var mappedCaptor =
        ArgumentCaptor.forClass(lab.autojournal.application.TaskCompletedEvent.class);

    taskEventConsumer.on(taskCompletedEvent);

    verify(taskEventHandler).handle(mappedCaptor.capture());
    assertThat(mappedCaptor.getValue().taskId()).isEqualTo(taskId);
  }

  @Test
  void on_taskCompletedWithEventId_mapsEventId() {
    var eventId = UUID.randomUUID();
    var taskCompletedEvent =
        TaskCompletedEvent.newBuilder()
            .setTaskId(randomId())
            .setEventId(eventId.toString())
            .build();
    var mappedCaptor =
        ArgumentCaptor.forClass(lab.autojournal.application.TaskCompletedEvent.class);

    taskEventConsumer.on(taskCompletedEvent);

    verify(taskEventHandler).handle(mappedCaptor.capture());
    assertThat(mappedCaptor.getValue().eventId()).contains(eventId);
  }

  @Test
  void on_taskCompletedWithoutEventId_mapsEmptyEventId() {
    var taskCompletedEvent = TaskCompletedEvent.newBuilder().setTaskId(randomId()).build();
    var mappedCaptor =
        ArgumentCaptor.forClass(lab.autojournal.application.TaskCompletedEvent.class);

    taskEventConsumer.on(taskCompletedEvent);

    verify(taskEventHandler).handle(mappedCaptor.capture());
    assertThat(mappedCaptor.getValue().eventId()).isEmpty();
  }

  @Test
  void on_handlesIgnoredMessages() {
    var taskAddedEvent = TaskAddedEvent.getDefaultInstance();

    taskEventConsumer.on(taskAddedEvent);
  }

  @Test
  void on_throwsForUnknownMessages() {
    var unknown = StringValue.getDefaultInstance();

    assertThrows(RuntimeException.class, () -> taskEventConsumer.on(unknown));
  }
}
