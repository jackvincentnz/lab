package lab.mops.config;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lab.libs.identity.Identity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Which issuer Mops trusts for the identity token that must accompany every request.
 *
 * @param issuer the value every accepted token must carry in {@code iss}.
 * @param jwkSetUri where the issuer publishes its signing keys.
 * @param audience the name this vertical expects in the token's {@code aud} claim.
 * @param development the identity to assume when a request carries no token. Off by default so a
 *     Mops instance rejects anything the issuer did not identify.
 */
@ConfigurationProperties("mops.identity")
public record IdentityProperties(
    @DefaultValue("lab-gateway") String issuer,
    @DefaultValue("http://localhost:3006/.well-known/jwks.json") String jwkSetUri,
    @DefaultValue("mops") String audience,
    @DefaultValue Development development) {

  public record Development(
      @DefaultValue("false") boolean enabled,
      @DefaultValue("00000000-0000-0000-0000-000000000001") UUID principalId,
      @DefaultValue("00000000-0000-0000-0000-000000000002") UUID tenantId,
      @DefaultValue({"mops:read", "mops:write"}) Set<String> scopes) {

    public Optional<Identity> identity() {
      return enabled ? Optional.of(new Identity(principalId, tenantId, scopes)) : Optional.empty();
    }
  }
}
