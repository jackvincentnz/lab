package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class GatewayPrincipalTest {
  private static final UUID PRINCIPAL = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID TENANT = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final String PASSWORD = "{bcrypt}test-hash";

  @Test
  void retainsIdentityAndMapsScopesToAuthorities() {
    var principal = new GatewayPrincipal(user(List.of("mops:read", "mops:write")));

    assertThat(principal.getUsername()).isEqualTo("alice");
    assertThat(principal.principal()).isEqualTo(PRINCIPAL);
    assertThat(principal.tenant()).isEqualTo(TENANT);
    assertThat(principal.scopes()).containsExactly("mops:read", "mops:write");
    assertThat(principal.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("SCOPE_mops:read", "SCOPE_mops:write");
  }

  @Test
  void grantsNoAuthoritiesWhenScopesAreEmpty() {
    var principal = new GatewayPrincipal(user(List.of()));

    assertThat(principal.scopes()).isEmpty();
    assertThat(principal.getAuthorities()).isEmpty();
  }

  @Test
  void erasingCredentialsPreservesIdentityAndOtherLoginInstances() {
    var configuredUser = user(List.of("mops:read"));
    var principal = new GatewayPrincipal(configuredUser);
    var otherLogin = new GatewayPrincipal(configuredUser);

    principal.eraseCredentials();

    assertThat(principal.getPassword()).isNull();
    assertThat(principal.principal()).isEqualTo(PRINCIPAL);
    assertThat(principal.tenant()).isEqualTo(TENANT);
    assertThat(principal.scopes()).containsExactly("mops:read");
    assertThat(principal.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("SCOPE_mops:read");
    assertThat(configuredUser.password()).isEqualTo(PASSWORD);
    assertThat(otherLogin.getPassword()).isEqualTo(PASSWORD);
  }

  private GatewayUsers.ConfiguredUser user(List<String> scopes) {
    return new GatewayUsers.ConfiguredUser("alice", PASSWORD, PRINCIPAL, TENANT, scopes);
  }
}
