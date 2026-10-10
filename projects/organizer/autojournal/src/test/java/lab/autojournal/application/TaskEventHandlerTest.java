package lab.autojournal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskEventHandlerTest extends TestBase {

  @Mock TaskService taskService;

  @Mock JournalService journalService;

  @InjectMocks TaskEventHandler taskEventHandler;

  @Test
  void handle_taskCompleted_addsJournalEntryContainingTaskTitle() {
    var taskId = randomId();
    var taskCompletedEvent = new TaskCompletedEvent(taskId);
    var task = new Task(taskId, randomString());

    var entryCaptor = ArgumentCaptor.forClass(String.class);

    when(taskService.getTask(taskCompletedEvent.taskId())).thenReturn(task);

    taskEventHandler.handle(taskCompletedEvent);

    verify(journalService).addEntry(entryCaptor.capture());
    assertThat(entryCaptor.getValue()).contains(task.title());
  }

  @Test
  void handle_eventRedelivered_addsJournalEntryOnce() {
    var taskId = randomId();
    var taskCompletedEvent = new TaskCompletedEvent(taskId);

    when(taskService.getTask(taskId)).thenReturn(new Task(taskId, randomString()));

    taskEventHandler.handle(taskCompletedEvent);
    taskEventHandler.handle(new TaskCompletedEvent(taskId));

    verify(taskService).getTask(taskId);
    verify(journalService).addEntry(anyString());
  }

  @Test
  void handle_differentTaskCompleted_addsJournalEntryForEachTask() {
    var firstTask = new Task(randomId(), randomString());
    var secondTask = new Task(randomId(), randomString());

    when(taskService.getTask(firstTask.taskId())).thenReturn(firstTask);
    when(taskService.getTask(secondTask.taskId())).thenReturn(secondTask);

    taskEventHandler.handle(new TaskCompletedEvent(firstTask.taskId()));
    taskEventHandler.handle(new TaskCompletedEvent(secondTask.taskId()));

    var entryCaptor = ArgumentCaptor.forClass(String.class);
    verify(journalService, times(2)).addEntry(entryCaptor.capture());
    assertThat(entryCaptor.getAllValues().get(0)).contains(firstTask.title());
    assertThat(entryCaptor.getAllValues().get(1)).contains(secondTask.title());
  }

  @Test
  void handle_redeliveredAfterAddEntryFailed_addsJournalEntry() {
    var taskId = randomId();
    var taskCompletedEvent = new TaskCompletedEvent(taskId);

    when(taskService.getTask(taskId)).thenReturn(new Task(taskId, randomString()));
    doThrow(new RuntimeException(randomString()))
        .doNothing()
        .when(journalService)
        .addEntry(anyString());

    assertThatThrownBy(() -> taskEventHandler.handle(taskCompletedEvent))
        .isInstanceOf(RuntimeException.class);
    taskEventHandler.handle(taskCompletedEvent);

    verify(journalService, times(2)).addEntry(anyString());
  }

  @Test
  void handle_moreTasksThanBound_handlesEvictedTaskAgain() {
    var firstTaskId = randomId();
    when(taskService.getTask(anyString()))
        .thenAnswer(invocation -> new Task(invocation.getArgument(0), randomString()));

    taskEventHandler.handle(new TaskCompletedEvent(firstTaskId));
    for (var i = 0; i < TaskEventHandler.MAX_HANDLED_TASK_IDS; i++) {
      taskEventHandler.handle(new TaskCompletedEvent(randomId()));
    }
    taskEventHandler.handle(new TaskCompletedEvent(firstTaskId));

    verify(taskService, times(2)).getTask(firstTaskId);
  }
}
