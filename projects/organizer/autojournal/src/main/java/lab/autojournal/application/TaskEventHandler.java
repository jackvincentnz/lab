package lab.autojournal.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lab.autojournal.domain.TaskCompletedEntry;
import org.springframework.stereotype.Service;

@Service
public class TaskEventHandler {

  /*
   * Kafka delivers at least once, so a completed event can arrive again. A task completes at most
   * once and the event carries only its task id, so the task id identifies the completion.
   *
   * The journal has no field to look an entry up by task id, so recently handled ids are kept in
   * memory instead. They do not survive a restart and are not shared across instances. The bound
   * caps memory at a few hundred kilobytes of ids while covering redeliveries, which arrive
   * shortly after the first delivery.
   */
  static final int MAX_HANDLED_TASK_IDS = 10_000;

  private final Set<String> handledTaskIds =
      Collections.newSetFromMap(
          Collections.synchronizedMap(
              new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                  return size() > MAX_HANDLED_TASK_IDS;
                }
              }));

  private final TaskService taskService;

  private final JournalService journalService;

  public TaskEventHandler(TaskService taskService, JournalService journalService) {
    this.taskService = taskService;
    this.journalService = journalService;
  }

  public void handle(TaskCompletedEvent taskCompletedEvent) {
    var taskId = taskCompletedEvent.taskId();
    if (handledTaskIds.contains(taskId)) {
      return;
    }

    var task = taskService.getTask(taskId);

    var entry = TaskCompletedEntry.from(task.title());

    journalService.addEntry(entry.getMessage());

    // Recorded only after the entry is added, so a failed attempt is retried rather than skipped.
    handledTaskIds.add(taskId);
  }
}
