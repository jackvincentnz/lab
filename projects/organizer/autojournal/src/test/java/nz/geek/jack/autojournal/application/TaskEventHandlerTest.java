package nz.geek.jack.autojournal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskEventHandlerTest {

  @Mock TaskService taskService;

  @Mock JournalService journalService;

  @InjectMocks TaskEventHandler taskEventHandler;

  @Test
  void handle_addsJournalEntryContainingTaskTitle_whenTaskCompleted() {
    var taskId = "taskId";
    var taskCompletedEvent = new TaskCompletedEvent(taskId);
    var task = new Task(taskId, "task title");

    var entryCaptor = ArgumentCaptor.forClass(String.class);

    when(taskService.getTask(taskCompletedEvent.taskId())).thenReturn(task);

    taskEventHandler.handle(taskCompletedEvent);

    verify(journalService).addEntry(entryCaptor.capture());
    assertThat(entryCaptor.getValue()).contains(task.title());
  }
}
