package nz.geek.jack.task.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TaskTest {

  @Test
  void addTask_setsTitle() {
    var title = "My Task";

    var task = Task.addTask(title);

    assertThat(task.getTitle()).isEqualTo(title);
  }

  @Test
  void addTask_setsId() {
    var title = "My Task";

    var task = Task.addTask(title);

    assertThat(task.getId()).isNotNull();
  }

  @Test
  void addTask_setsCreatedAt() {
    var title = "My Task";

    var task = Task.addTask(title);

    assertThat(task.getCreatedAt()).isBetween(Instant.now().minusSeconds(10), Instant.now());
  }

  @Test
  void addTask_appliesTaskAddedEvent() {
    var title = "My Task";

    var task = Task.addTask(title);

    var events = task.flushEvents();
    var event = events.get(0);

    assertThat(events.size()).isEqualTo(1);
    assertThat(event).isInstanceOf(TaskAddedEvent.class);
  }

  @Test
  void addTask_appliesTaskAddedEvent_withId() {
    var title = "My Task";

    var task = Task.addTask(title);

    var events = task.flushEvents();
    var event = (TaskAddedEvent) events.get(0);

    assertThat(event.getTaskId()).isEqualTo(task.getId());
  }

  @Test
  void addTask_appliesTaskAddedEvent_withTitle() {
    var title = "My Task";

    var task = Task.addTask(title);

    var events = task.flushEvents();
    var event = (TaskAddedEvent) events.get(0);

    assertThat(event.getTitle()).isEqualTo(title);
  }

  @Test
  void addTask_appliesTaskAddedEvent_withCreatedAt() {
    var title = "My Task";

    var task = Task.addTask(title);

    var events = task.flushEvents();
    var event = (TaskAddedEvent) events.get(0);

    assertThat(event.getCreatedAt()).isEqualTo(task.getCreatedAt());
  }

  @Test
  void markCompleted_setsCompleted() {
    var title = "My Task";
    var task = Task.addTask(title);

    task.markCompleted();

    assertThat(task.isCompleted()).isTrue();
  }

  @Test
  void markCompleted_appliesTaskCompletedEvent() {
    var title = "My Task";
    var task = Task.addTask(title);
    task.flushEvents();

    task.markCompleted();

    var events = task.flushEvents();
    var event = events.get(0);

    assertThat(events.size()).isEqualTo(1);
    assertThat(event).isInstanceOf(TaskCompletedEvent.class);
  }

  @Test
  void markCompleted_isIdempotent() {
    var title = "My Task";
    var task = Task.addTask(title);
    task.flushEvents();

    task.markCompleted();
    task.markCompleted();

    var events = task.flushEvents();

    assertThat(events.size()).isEqualTo(1);
  }

  @Test
  void markCompleted_appliesTaskCompletedEvent_withId() {
    var title = "My Task";
    var task = Task.addTask(title);
    task.flushEvents();

    task.markCompleted();

    var events = task.flushEvents();
    var event = (TaskCompletedEvent) events.get(0);

    assertThat(event.getTaskId()).isEqualTo(task.getId());
  }

  @Test
  void of_setsId() {
    var id = TaskId.create();
    var title = "My Task";
    var isCompleted = false;
    var createdAt = Instant.now();

    var task = Task.of(id, title, isCompleted, createdAt);

    assertThat(task.getId()).isEqualTo(id);
  }

  @Test
  void of_setsTitle() {
    var id = TaskId.create();
    var title = "My Task";
    var isCompleted = false;
    var createdAt = Instant.now();

    var task = Task.of(id, title, isCompleted, createdAt);

    assertThat(task.getTitle()).isEqualTo(title);
  }

  @Test
  void of_setsIsCompleted() {
    var id = TaskId.create();
    var title = "My Task";
    var isCompleted = true;
    var createdAt = Instant.now();

    var task = Task.of(id, title, isCompleted, createdAt);

    assertThat(task.isCompleted()).isTrue();
  }

  @Test
  void of_setsCreatedAt() {
    var id = TaskId.create();
    var title = "My Task";
    var isCompleted = true;
    var createdAt = Instant.now();

    var task = Task.of(id, title, isCompleted, createdAt);

    assertThat(task.getCreatedAt()).isEqualTo(createdAt);
  }
}
