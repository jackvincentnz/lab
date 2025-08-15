package nz.geek.jack.libs.ddd.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class AggregateTest extends TestBase {

  @Test
  void constructor_preventsNullId() {
    assertThatThrownBy(() -> new TestAggregate(null)).isInstanceOf(NullPointerException.class);
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

  static class TestAggregate extends Aggregate<TestId> {
    TestAggregate(TestId id) {
      super(id);
    }

    void addEvent(Object event) {
      registerEvent(event);
    }
  }

  static class TestId extends AbstractId {
    TestId() {
      super();
    }
  }
}
