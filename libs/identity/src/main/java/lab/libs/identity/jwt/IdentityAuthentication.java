package lab.libs.identity.jwt;

import java.util.Objects;
import lab.libs.identity.Identity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** A request authenticated as an identity resolved by a trusted issuer. */
public final class IdentityAuthentication extends AbstractAuthenticationToken {

  private final Identity identity;

  public IdentityAuthentication(Identity identity) {
    super(
        Objects.requireNonNull(identity, "identity").scopes().stream()
            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
            .toList());
    this.identity = identity;
    setAuthenticated(true);
  }

  public Identity identity() {
    return identity;
  }

  @Override
  public Identity getPrincipal() {
    return identity;
  }

  @Override
  public Object getCredentials() {
    return null;
  }
}
