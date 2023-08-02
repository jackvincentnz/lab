package nz.geek.jack.libs.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AggregateTest {

  @Test
  void apply_shouldApplyEvent() {
    var aggregate = new TestAggregate();

    var appliedEvents = aggregate.getAppliedEvents();

    assertThat(appliedEvents).hasSize(1);
    assertThat(appliedEvents.get(0)).isInstanceOf(TestAggregateCreatedEvent.class);
  }

  @Test
  void apply_shouldReduceState() {
    var aggregate = new TestAggregate();

    assertThat(aggregate.getState()).isEqualTo(TestAggregate.STATE_UPDATED_STRING);
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

    String getState() {
      return state;
    }
  }

  static final class TestAggregateCreatedEvent extends DomainEvent {}
}
