package lab.libs.identity;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The caller identity a vertical receives from the edge gateway.
 *
 * <p>The gateway authenticates the public request and resolves the caller to platform identifiers.
 * Verticals trust the resolved identity and own all domain authorization on top of it.
 */
public record Identity(UUID principalId, UUID tenantId, Set<String> scopes) {

  public Identity {
    Objects.requireNonNull(principalId, "principalId");
    Objects.requireNonNull(tenantId, "tenantId");
    scopes = Set.copyOf(Objects.requireNonNull(scopes, "scopes"));
  }

  public boolean hasScope(String scope) {
    return scopes.contains(scope);
  }
}
