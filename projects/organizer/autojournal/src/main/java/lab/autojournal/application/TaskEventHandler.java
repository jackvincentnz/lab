package lab.autojournal.application;

import lab.autojournal.domain.TaskCompletedEntry;
import org.springframework.stereotype.Service;

@Service
public class TaskEventHandler {

  private final TaskService taskService;

  private final JournalService journalService;

  public TaskEventHandler(TaskService taskService, JournalService journalService) {
    this.taskService = taskService;
    this.journalService = journalService;
  }

  public void handle(TaskCompletedEvent taskCompletedEvent) {
    // TODO: handle duplicate events
    var task = taskService.getTask(taskCompletedEvent.taskId());

    var entry = TaskCompletedEntry.from(task.title());

    journalService.addEntry(entry.getMessage());
  }
}
