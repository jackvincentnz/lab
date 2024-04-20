package nz.geek.jack.libs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AggregateTest {

  @Test
  void apply_shouldApplyEvent() {
    var aggregate = new TestAggregate();

    var appliedEvents = aggregate.flushEvents();

    assertThat(appliedEvents).hasSize(1);
    assertThat(appliedEvents.get(0)).isInstanceOf(TestAggregateCreatedEvent.class);
  }

  @Test
  void apply_shouldReduceState() {
    var aggregate = new TestAggregate();

    assertThat(aggregate.getState()).isEqualTo(TestAggregate.STATE_UPDATED_STRING);
  }

  @Test()
  void apply_throwsWhenReducerMethodIsMissing() {
    var aggregate = new TestAggregate();

    assertThrows(IllegalArgumentException.class, () -> aggregate.apply(new EventWithoutHandler()));
  }

  @Test()
  void apply_throwsWhenReducerMethodThrows() {
    var aggregate = new TestAggregate();

    assertThrows(
        EventReductionException.class, () -> aggregate.apply(new EventThatThrowsWhenHandled()));
  }

  @Test
  void flushEvents_shouldClearEvents() {
    var aggregate = new TestAggregate();

    var events = aggregate.flushEvents();
    var afterFlush = aggregate.flushEvents();

    assertThat(events).hasSize(1);
    assertThat(afterFlush).hasSize(0);
  }

  static final class TestAggregate extends Aggregate {

    private static final String STATE_UPDATED_STRING = "updated";

    private String state = "";

    TestAggregate() {
      apply(new TestAggregateCreatedEvent());
    }

    private void on(TestAggregateCreatedEvent testAggregateCreatedEvent) {
      state = STATE_UPDATED_STRING;
    }

    private void on(EventThatThrowsWhenHandled eventThatThrowsWhenHandled) {
      throw new RuntimeException("Couldn't handle it!");
    }

    String getState() {
      return state;
    }
  }

  static final class TestAggregateCreatedEvent extends DomainEvent {}

  static final class EventWithoutHandler extends DomainEvent {}

  static final class EventThatThrowsWhenHandled extends DomainEvent {}
}
