package lab.libs.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentityTest {

  @Test
  void hasScope_matchesGrantedScopes() {
    var identity = new Identity(UUID.randomUUID(), UUID.randomUUID(), Set.of("mops:read"));

    assertThat(identity.hasScope("mops:read")).isTrue();
    assertThat(identity.hasScope("mops:write")).isFalse();
  }

  @Test
  void scopes_areCopiedAndImmutable() {
    var scopes = new HashSet<>(Set.of("mops:read"));
    var identity = new Identity(UUID.randomUUID(), UUID.randomUUID(), scopes);

    scopes.add("mops:write");

    assertThat(identity.scopes()).containsExactly("mops:read");
    assertThatThrownBy(() -> identity.scopes().add("mops:write"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void constructor_rejectsNulls() {
    var id = UUID.randomUUID();

    assertThatThrownBy(() -> new Identity(null, id, Set.of()))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new Identity(id, null, Set.of()))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new Identity(id, id, null)).isInstanceOf(NullPointerException.class);
  }
}
