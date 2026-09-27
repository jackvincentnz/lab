package lab.gateway;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Serializable identity retained in the server-side session after authentication. */
public final class GatewayPrincipal extends User {
  private static final long serialVersionUID = 1L;

  private final UUID principal;
  private final UUID tenant;
  private final List<String> scopes;

  public GatewayPrincipal(GatewayUsers.ConfiguredUser user) {
    super(
        user.username(),
        user.password(),
        user.scopes().stream().map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)).toList());
    principal = user.principal();
    tenant = user.tenant();
    scopes = user.scopes();
  }

  public UUID principal() {
    return principal;
  }

  public UUID tenant() {
    return tenant;
  }

  public List<String> scopes() {
    return scopes;
  }
}
