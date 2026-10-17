package lab.libs.identity.jwt;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lab.libs.identity.Identity;
import org.springframework.security.oauth2.jwt.Jwt;

/** Claims of the identity token a trusted issuer mints for the service it is addressed to. */
public final class IdentityClaims {

  /** Active tenant UUID. */
  public static final String TENANT = "tenant";

  /** Space-separated scopes granted to the session for this service. */
  public static final String SCOPE = "scope";

  /** How the caller authenticated at the issuer: {@code form}, {@code oidc}, or {@code bearer}. */
  public static final String AUTHENTICATION_METHODS = "amr";

  private IdentityClaims() {}

  /** Builds the identity from a token that {@link IdentityClaimsValidator} has accepted. */
  public static Identity identity(Jwt jwt) {
    return new Identity(
        UUID.fromString(jwt.getSubject()),
        UUID.fromString(jwt.getClaimAsString(TENANT)),
        scopes(jwt.getClaimAsString(SCOPE)));
  }

  static Set<String> scopes(String scope) {
    return Arrays.stream(scope.trim().split("\\s+"))
        .filter(value -> !value.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
  }
}
