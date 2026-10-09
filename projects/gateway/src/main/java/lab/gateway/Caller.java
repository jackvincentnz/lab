package lab.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lab.libs.identity.jwt.IdentityClaims;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Who the gateway authenticated for one request and how, whichever public credential it came in on.
 * The identity token is minted from this, so adding a credential type means adding a conversion
 * here and nothing downstream changes.
 *
 * @param principal the principal UUID.
 * @param tenant the active tenant UUID.
 * @param scopes the scopes granted to the caller.
 * @param authenticationMethod how the caller authenticated at the gateway, as the token's {@code
 *     amr} value.
 */
public record Caller(
    UUID principal, UUID tenant, List<String> scopes, String authenticationMethod) {

  /** The {@code amr} value for a session established by form login. */
  public static final String FORM = "form";

  /** The {@code amr} value for a client bearer JWT the gateway verified. */
  public static final String BEARER = "bearer";

  /** The caller behind an authentication, or empty when the gateway did not establish one. */
  public static Optional<Caller> of(Authentication authentication) {
    if (authentication.getPrincipal() instanceof GatewayPrincipal principal) {
      return Optional.of(
          new Caller(principal.principal(), principal.tenant(), principal.scopes(), FORM));
    }
    if (authentication instanceof JwtAuthenticationToken bearer) {
      // The decoder has already checked these claims are UUIDs and a non-blank scope string.
      var jwt = bearer.getToken();
      return Optional.of(
          new Caller(
              UUID.fromString(jwt.getSubject()),
              UUID.fromString(jwt.getClaimAsString(IdentityClaims.TENANT)),
              List.of(jwt.getClaimAsString(IdentityClaims.SCOPE).trim().split("\\s+")),
              BEARER));
    }
    return Optional.empty();
  }
}
