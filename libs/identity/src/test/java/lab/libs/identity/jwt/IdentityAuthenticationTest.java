package lab.libs.identity.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;
import lab.libs.identity.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class IdentityAuthenticationTest {

  private final Identity identity =
      new Identity(UUID.randomUUID(), UUID.randomUUID(), Set.of("mops:read", "mops:write"));

  @Test
  void constructor_isAuthenticatedAsTheIdentity() {
    var authentication = new IdentityAuthentication(identity);

    assertThat(authentication.isAuthenticated()).isTrue();
    assertThat(authentication.identity()).isSameAs(identity);
    assertThat(authentication.getPrincipal()).isSameAs(identity);
    assertThat(authentication.getCredentials()).isNull();
  }

  @Test
  void getAuthorities_areTheScopesWithTheScopePrefix() {
    var authentication = new IdentityAuthentication(identity);

    assertThat(authentication.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("SCOPE_mops:read", "SCOPE_mops:write");
  }

  @Test
  void constructor_rejectsNullIdentity() {
    assertThatThrownBy(() -> new IdentityAuthentication(null))
        .isInstanceOf(NullPointerException.class);
  }
}
