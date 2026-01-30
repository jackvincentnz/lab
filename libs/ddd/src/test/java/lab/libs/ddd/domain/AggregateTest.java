package lab.libs.ddd.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class AggregateTest extends TestBase {

  @Test
  void constructor_preventsNullId() {
    assertThatThrownBy(() -> new TestAggregate(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void constructor_setsCreatedAt() {
    var clock = fixedClock();

    var aggregate = new TestAggregate(new TestId(), clock);

    assertThat(aggregate.getCreatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void constructor_updatedAtEqualsCreatedAt() {
    var aggregate = new TestAggregate(new TestId());

    assertThat(aggregate.getUpdatedAt()).isNotNull();
    assertThat(aggregate.getUpdatedAt()).isEqualTo(aggregate.getCreatedAt());
  }

  @Test
  void getId_returnsId() {
    var id = new TestId();
    var aggregate = new TestAggregate(id);

    assertThat(aggregate.getId()).isEqualTo(id);
  }

  @Test
  void getVersion_returnsVersion() {
    var version = randomInt();
    var aggregate = new TestAggregate(new TestId());
    aggregate.version = version;

    assertThat(aggregate.getVersion()).isEqualTo(version);
  }

  @Test
  void domainEvents_returnsDomainEvents() {
    var event = new Object();
    var aggregate = new TestAggregate(new TestId());
    aggregate.addEvent(event);

    assertThat(aggregate.domainEvents()).hasSize(1);
    assertThat(aggregate.domainEvents()).contains(event);
  }

  @Test
  void registerEvent_updatesUpdatedAt() {
    var initialClock = fixedClock();
    var updatedClock = fixedClock(Instant.MAX);

    var aggregate = new TestAggregate(new TestId(), initialClock);

    aggregate.setClock(updatedClock);
    aggregate.registerEvent(new Object());

    assertThat(aggregate.getUpdatedAt()).isEqualTo(updatedClock.instant());
  }

  static class TestAggregate extends Aggregate<TestId> {
    TestAggregate(TestId id) {
      super(id);
    }

    TestAggregate(TestId id, Clock clock) {
      super(id, clock);
    }

    void addEvent(Object event) {
      registerEvent(event);
    }
  }

  static class TestId extends InternalId {
    TestId() {
      super();
    }
  }
}
