package lab.libs.ddd.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class BaseRepositoryTest extends TestBase {

  @SuppressWarnings("unchecked")
  @Test
  void getById_throwsWhenNotFound() {
    var repository = spy(BaseRepository.class);
    var id = new TestId();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> repository.getById(id));
  }

  @SuppressWarnings("unchecked")
  @Test
  void mapById_maps() {
    var repository = spy(BaseRepository.class);
    var aggregate = new TestAggregate(new TestId());
    var ids = Set.of(aggregate.getId());
    when(repository.findAllById(ids)).thenReturn(List.of(aggregate));

    var result = repository.mapById(ids);

    assertThat(result).hasSize(1);
    assertThat(result.get(aggregate.getId())).isEqualTo(aggregate);
  }

  static class TestId extends InternalId {
    TestId() {
      super();
    }
  }

  static class TestAggregate extends Aggregate<TestId> {
    TestAggregate(TestId id) {
      super(id);
    }
  }
}
