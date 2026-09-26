package lab.mops.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lab.libs.identity.Identity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * How Mops resolves the caller identity from the gateway contract.
 *
 * @param publicPaths Ant-style paths that never require an identity.
 * @param development the identity to assume when a request carries no contract headers. Off by
 *     default so a Mops instance behind the gateway rejects anything the gateway did not identify.
 */
@ConfigurationProperties("mops.identity")
public record IdentityProperties(
    @DefaultValue({"/actuator/health", "/actuator/health/**"}) List<String> publicPaths,
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
