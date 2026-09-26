package lab.libs.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class IdentityHolderTest {

  @AfterEach
  void clear() {
    IdentityHolder.clear();
  }

  @Test
  void get_isEmpty_whenNothingSet() {
    assertThat(IdentityHolder.get()).isEmpty();
  }

  @Test
  void get_returnsIdentity_afterSet() {
    var identity = new Identity(UUID.randomUUID(), UUID.randomUUID(), Set.of("mops:read"));

    IdentityHolder.set(identity);

    assertThat(IdentityHolder.get()).contains(identity);
  }

  @Test
  void get_isEmpty_afterClear() {
    IdentityHolder.set(new Identity(UUID.randomUUID(), UUID.randomUUID(), Set.of()));

    IdentityHolder.clear();

    assertThat(IdentityHolder.get()).isEmpty();
  }
}
