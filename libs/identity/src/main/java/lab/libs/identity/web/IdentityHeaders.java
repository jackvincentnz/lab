package lab.libs.identity.web;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lab.libs.identity.Identity;

/**
 * The downstream identity contract: the headers the edge gateway injects on every request it
 * forwards to a vertical.
 */
public final class IdentityHeaders {

  public static final String PRINCIPAL_ID = "X-Principal-Id";
  public static final String TENANT_ID = "X-Tenant-Id";
  public static final String SCOPES = "X-Scopes";

  private IdentityHeaders() {}

  /** Outcome of reading the contract headers from a request. */
  public sealed interface Result permits Absent, Invalid, Present {}

  /** None of the contract headers were sent. */
  public record Absent() implements Result {}

  /** At least one header was sent but the set does not form a valid identity. */
  public record Invalid(String reason) implements Result {}

  /** All headers were sent and parsed. */
  public record Present(Identity identity) implements Result {}

  public static Result parse(String principalId, String tenantId, String scopes) {
    if (principalId == null && tenantId == null && scopes == null) {
      return new Absent();
    }

    UUID principal;
    UUID tenant;
    try {
      principal = parseUuid(PRINCIPAL_ID, principalId);
      tenant = parseUuid(TENANT_ID, tenantId);
    } catch (IllegalArgumentException e) {
      return new Invalid(e.getMessage());
    }

    if (scopes == null) {
      return new Invalid("missing " + SCOPES);
    }
    Set<String> parsedScopes =
        Arrays.stream(scopes.trim().split("\\s+"))
            .filter(scope -> !scope.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    if (parsedScopes.isEmpty()) {
      return new Invalid("empty " + SCOPES);
    }

    return new Present(new Identity(principal, tenant, parsedScopes));
  }

  private static UUID parseUuid(String header, String value) {
    if (value == null) {
      throw new IllegalArgumentException("missing " + header);
    }
    try {
      return UUID.fromString(value.trim());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("malformed " + header);
    }
  }
}
