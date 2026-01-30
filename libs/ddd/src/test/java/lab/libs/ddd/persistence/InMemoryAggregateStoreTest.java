package lab.libs.ddd.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lab.libs.ddd.domain.EventSourcedAggregate;
import lab.libs.ddd.domain.InternalId;
import org.junit.jupiter.api.Test;

class InMemoryAggregateStoreTest {

  TestRepository testRepository = new TestRepository();

  @Test
  void save_storesAggregatesById() {
    var aggregate = new TestAggregate();

    testRepository.save(aggregate);

    var retrieved = testRepository.get(aggregate.getId());
    assertThat(retrieved).isEqualTo(aggregate);
  }

  @Test
  void getAll_retrievesStoredAggregates() {
    var a1 = new TestAggregate();
    var a2 = new TestAggregate();
    var a3 = new TestAggregate();

    testRepository.save(a1);
    testRepository.save(a2);
    testRepository.save(a3);

    var aggregates = testRepository.getAll();

    assertThat(aggregates).hasSize(3);
    assertThat(aggregates).containsAll(List.of(a1, a2, a3));
  }

  @Test
  void get_throwsForMissingAggregate() {
    assertThrows(RuntimeException.class, () -> testRepository.get(new TestId()));
  }

  static final class TestRepository extends InMemoryAggregateStore<TestId, TestAggregate> {}

  static final class TestAggregate extends EventSourcedAggregate<TestId> {
    TestAggregate() {
      id = new TestId();
    }
  }

  static final class TestId extends InternalId {
    TestId() {
      super();
    }
  }
}
