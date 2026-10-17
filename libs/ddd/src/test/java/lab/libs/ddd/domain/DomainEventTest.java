package lab.libs.ddd.domain;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class DomainEventTest extends TestBase {

  @Test
  void constructor_newEvent_assignsEventId() {
    var event = new TestEvent(new TestId());

    assertThat(event.getEventId()).isNotNull();
  }

  @Test
  void constructor_eventsWithSameAggregate_assignsDistinctEventIds() {
    var aggregateId = new TestId();

    var first = new TestEvent(aggregateId);
    var second = new TestEvent(aggregateId);

    assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
  }

  static final class TestEvent extends DomainEvent<TestId> {
    TestEvent(TestId aggregateId) {
      super(aggregateId);
    }
  }

  static final class TestId extends InternalId {}
}
