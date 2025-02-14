package nz.geek.jack.libs.ddd.domain;

import static nz.geek.jack.libs.ddd.domain.test.ESAggregateTestUtils.getOnlyEventOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class EventSourcedAggregateTest {

  @Test
  void apply_shouldApplyEvent() {
    var aggregate = TestAggregate.create();

    getOnlyEventOfType(aggregate, TestAggregateCreatedEvent.class);
  }

  @Test
  void apply_shouldReduceState() {
    var aggregate = TestAggregate.create();

    assertThat(aggregate.getId()).isEqualTo(TestAggregate.EXPECTED_ID);
  }

  @Test()
  void apply_throwsWhenReducerMethodIsMissing() {
    var aggregate = TestAggregate.create();

    assertThrows(
        IllegalArgumentException.class,
        () -> aggregate.apply(new EventWithoutHandler(aggregate.getId())));
  }

  @Test()
  void apply_throwsWhenReducerMethodThrows() {
    var aggregate = TestAggregate.create();

    assertThrows(
        EventReductionException.class,
        () -> aggregate.apply(new EventThatThrowsWhenHandled(aggregate.getId())));
  }

  @Test
  void flushEvents_shouldClearEvents() {
    var aggregate = TestAggregate.create();

    var events = aggregate.flushEvents();
    var afterFlush = aggregate.flushEvents();

    assertThat(events).hasSize(1);
    assertThat(afterFlush).hasSize(0);
  }

  @Test
  void replay_throwsIfChanges() {
    var aggregate = TestAggregate.create();

    assertThrows(IllegalStateException.class, () -> aggregate.replay(List.of()));
  }

  @Test
  void replay_shouldReduceState() {
    var aggregate = new TestAggregate();

    assertThat(aggregate.getId()).isNull();

    aggregate.replay(List.of(new TestAggregateCreatedEvent(TestAggregate.EXPECTED_ID)));

    assertThat(aggregate.getId()).isEqualTo(TestAggregate.EXPECTED_ID);
  }

  @Test
  void replay_shouldIncrementVersion() {
    var aggregate = new TestAggregate();

    assertThat(aggregate.getVersion()).isEqualTo(0);

    aggregate.replay(
        List.of(
            new TestAggregateCreatedEvent(TestAggregate.EXPECTED_ID),
            new TestAggregateEvent(TestAggregate.EXPECTED_ID),
            new TestAggregateEvent(TestAggregate.EXPECTED_ID)));

    assertThat(aggregate.getVersion()).isEqualTo(3);
  }

  static final class TestAggregate extends EventSourcedAggregate<TestId> {
    private static final TestId EXPECTED_ID = new TestId();

    static TestAggregate create() {
      var aggregate = new TestAggregate();
      aggregate.apply(new TestAggregateCreatedEvent(EXPECTED_ID));
      return aggregate;
    }

    private void on(TestAggregateCreatedEvent testAggregateCreatedEvent) {
      id = testAggregateCreatedEvent.aggregateId;
    }

    private void on(EventThatThrowsWhenHandled eventThatThrowsWhenHandled) {
      throw new RuntimeException("Couldn't handle it!");
    }

    private void on(TestAggregateEvent event) {
      // do nothing
    }
  }

  static final class TestId extends AbstractId {
    TestId() {
      super();
    }
  }

  static final class TestAggregateCreatedEvent extends DomainEvent<TestId> {

    TestAggregateCreatedEvent(TestId id) {
      super(id);
    }
  }

  static final class TestAggregateEvent extends DomainEvent<TestId> {

    TestAggregateEvent(TestId id) {
      super(id);
    }
  }

  static final class EventWithoutHandler extends DomainEvent<TestId> {
    EventWithoutHandler(TestId id) {
      super(id);
    }
  }

  static final class EventThatThrowsWhenHandled extends DomainEvent<TestId> {
    EventThatThrowsWhenHandled(TestId id) {
      super(id);
    }
  }
}
