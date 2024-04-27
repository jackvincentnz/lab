package nz.geek.jack.libs.ddd.domain;

import static nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils.getOnlyEventOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AggregateTest {

  @Test
  void apply_shouldApplyEvent() {
    var aggregate = new TestAggregate();

    getOnlyEventOfType(aggregate, TestAggregateCreatedEvent.class);
  }

  @Test
  void apply_shouldReduceState() {
    var aggregate = new TestAggregate();

    assertThat(aggregate.getId()).isEqualTo(TestAggregate.EXPECTED_ID);
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

  static final class TestAggregate extends Aggregate<TestId> {
    private static final TestId EXPECTED_ID = new TestId();

    TestAggregate() {
      apply(new TestAggregateCreatedEvent(EXPECTED_ID));
    }

    private void on(TestAggregateCreatedEvent testAggregateCreatedEvent) {
      id = testAggregateCreatedEvent.id;
    }

    private void on(EventThatThrowsWhenHandled eventThatThrowsWhenHandled) {
      throw new RuntimeException("Couldn't handle it!");
    }
  }

  static final class TestId extends AbstractId {
    TestId() {
      super();
    }
  }

  static final class TestAggregateCreatedEvent extends DomainEvent {
    private final TestId id;

    TestAggregateCreatedEvent(TestId id) {
      this.id = id;
    }
  }

  static final class EventWithoutHandler extends DomainEvent {}

  static final class EventThatThrowsWhenHandled extends DomainEvent {}
}
