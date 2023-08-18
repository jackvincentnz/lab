package nz.geek.jack.task.application.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskCommandServiceTest {

  @Mock TaskRepository taskRepository;

  @InjectMocks private TaskCommandService taskCommandService;

  @Test
  void addTask_savesTaskWithTitle() {
    var title = "My Task";
    var taskCaptor = ArgumentCaptor.forClass(Task.class);

    taskCommandService.addTask(title);

    verify(taskRepository).saveTask(taskCaptor.capture());
    assertThat(taskCaptor.getValue().getTitle()).isEqualTo(title);
  }

  @Test
  void addTask_returnsSavedTaskId() {
    var title = "My Task";
    var taskCaptor = ArgumentCaptor.forClass(Task.class);

    var taskId = taskCommandService.addTask(title);

    verify(taskRepository).saveTask(taskCaptor.capture());
    assertThat(taskId).isEqualTo(taskCaptor.getValue().getId());
  }

  @Test
  void markTaskCompleted_savesCompletedTask() {
    var task = Task.addTask("My Task");
    when(taskRepository.getTask(task.getId())).thenReturn(task);
    var taskCaptor = ArgumentCaptor.forClass(Task.class);

    assertThat(task.isCompleted()).isFalse();

    taskCommandService.markTaskCompleted(task.getId());

    verify(taskRepository).saveTask(taskCaptor.capture());
    assertThat(taskCaptor.getValue()).isEqualTo(task);
    assertThat(task.isCompleted()).isTrue();
  }
}
