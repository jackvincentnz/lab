package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.UUID;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class GatewayUsersTest extends TestBase {

  @Test
  void configuredUser_acceptsEncoderPrefixedPassword() {
    assertThatCode(() -> user(randomString(), "{noop}" + randomString()))
        .doesNotThrowAnyException();
    assertThatCode(() -> user(randomString(), "{bcrypt}$2a$10$" + randomString()))
        .doesNotThrowAnyException();
  }

  @Test
  void configuredUser_rejectsPasswordWithoutEncoderId() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> user(randomString(), "$2a$10$" + randomString()))
        .withMessageContaining("{id}encodedPassword");
  }

  @Test
  void gatewayUsers_rejectsDuplicateUsernames() {
    var username = randomString();

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new GatewayUsers(
                    List.of(
                        user(username, "{noop}" + randomString()),
                        user(username, "{noop}" + randomString()))))
        .withMessageContaining("unique");
  }

  @Test
  void gatewayUsers_defaultsToNoUsers() {
    assertThat(new GatewayUsers(null).users()).isEmpty();
  }

  private GatewayUsers.ConfiguredUser user(String username, String password) {
    return new GatewayUsers.ConfiguredUser(
        username, password, UUID.fromString(randomId()), UUID.fromString(randomId()), List.of());
  }
}
