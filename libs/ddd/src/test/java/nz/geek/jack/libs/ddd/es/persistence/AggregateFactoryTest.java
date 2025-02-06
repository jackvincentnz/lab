package nz.geek.jack.libs.ddd.es.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nz.geek.jack.libs.ddd.domain.AbstractId;
import nz.geek.jack.libs.ddd.domain.EventSourcedAggregate;
import org.junit.jupiter.api.Test;

class AggregateFactoryTest {

  AggregateFactory aggregateFactory = new AggregateFactory();

  @Test
  void forClass_returnsNewInstance() {
    var result = aggregateFactory.forClass(TestAggregate.class);

    assertThat(result).isInstanceOf(TestAggregate.class);
  }

  @Test
  void forClass_throwsForMissingEmptyConstructor() {
    assertThrows(
        RuntimeException.class,
        () -> aggregateFactory.forClass(TestAggregateWithNoConstructor.class));
  }

  static class TestAggregate extends EventSourcedAggregate<TestId> {}

  static class TestAggregateWithNoConstructor extends EventSourcedAggregate<TestId> {
    private TestAggregateWithNoConstructor(String something) {}
  }

  static class TestId extends AbstractId {}
}
