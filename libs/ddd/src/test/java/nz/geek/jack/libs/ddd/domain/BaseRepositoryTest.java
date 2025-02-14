package nz.geek.jack.libs.ddd.domain;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nz.geek.jack.test.TestBase;
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

  static class TestId extends AbstractId {
    TestId() {
      super();
    }
  }
}
