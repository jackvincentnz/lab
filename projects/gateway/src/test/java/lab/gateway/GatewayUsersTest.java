package lab.gateway;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GatewayUsersTest {
  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Test
  void acceptsEncoderPrefixedPasswords() {
    assertThatCode(() -> user("alice", "{noop}secret")).doesNotThrowAnyException();
    assertThatCode(() -> user("alice", "{bcrypt}$2a$10$hash")).doesNotThrowAnyException();
  }

  @Test
  void rejectsPasswordWithoutEncoderId() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> user("alice", "$2a$10$hash"))
        .withMessageContaining("{id}encodedPassword");
  }

  @Test
  void rejectsDuplicateUsernames() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> new GatewayUsers(List.of(user("alice", "{noop}a"), user("alice", "{noop}b"))))
        .withMessageContaining("unique");
  }

  private static GatewayUsers.ConfiguredUser user(String username, String password) {
    return new GatewayUsers.ConfiguredUser(username, password, ID, ID, List.of());
  }
}
