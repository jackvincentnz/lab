package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class GatewayPrincipalTest extends TestBase {

  @Test
  void principal_retainsConfiguredIdentity() {
    var user = randomUser(List.of(randomString()));

    var principal = new GatewayPrincipal(user);

    assertThat(principal.getUsername()).isEqualTo(user.username());
    assertThat(principal.principal()).isEqualTo(user.principal());
    assertThat(principal.tenant()).isEqualTo(user.tenant());
    assertThat(principal.scopes()).isEqualTo(user.scopes());
  }

  @Test
  void getAuthorities_prefixesEachScope() {
    var read = randomString();
    var write = randomString();

    var principal = new GatewayPrincipal(randomUser(List.of(read, write)));

    assertThat(principal.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("SCOPE_" + read, "SCOPE_" + write);
  }

  @Test
  void getAuthorities_isEmptyWithoutScopes() {
    var principal = new GatewayPrincipal(randomUser(List.of()));

    assertThat(principal.getAuthorities()).isEmpty();
  }

  @Test
  void eraseCredentials_clearsOnlyThatLoginsPassword() {
    var user = randomUser(List.of(randomString()));
    var principal = new GatewayPrincipal(user);
    var otherLogin = new GatewayPrincipal(user);

    principal.eraseCredentials();

    assertThat(principal.getPassword()).isNull();
    assertThat(principal.principal()).isEqualTo(user.principal());
    assertThat(user.password()).isNotNull();
    assertThat(otherLogin.getPassword()).isEqualTo(user.password());
  }

  private GatewayUsers.ConfiguredUser randomUser(List<String> scopes) {
    return new GatewayUsers.ConfiguredUser(
        randomString(),
        "{noop}" + randomString(),
        UUID.fromString(randomId()),
        UUID.fromString(randomId()),
        scopes);
  }
}
