package lab.libs.identity.jwt;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

/**
 * Checks that a signed token carries the identity contract for one vertical: the trusted issuer,
 * this vertical in the audience, an expiry, UUID principal and tenant, and a scope string.
 */
public final class IdentityClaimsValidator implements OAuth2TokenValidator<Jwt> {

  private final String issuer;
  private final String audience;

  public IdentityClaimsValidator(String issuer, String audience) {
    this.issuer = Objects.requireNonNull(issuer, "issuer");
    this.audience = Objects.requireNonNull(audience, "audience");
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    if (!issuer.equals(jwt.getClaimAsString(JwtClaimNames.ISS))) {
      return failure("iss is not " + issuer);
    }
    List<String> audiences = jwt.getAudience();
    if (audiences == null || !audiences.contains(audience)) {
      return failure("aud does not contain " + audience);
    }
    if (jwt.getExpiresAt() == null) {
      return failure("exp is missing");
    }
    if (!isUuid(jwt.getSubject())) {
      return failure("sub is not a UUID");
    }
    if (!isUuid(jwt.getClaimAsString(IdentityClaims.TENANT))) {
      return failure(IdentityClaims.TENANT + " is not a UUID");
    }
    if (!(jwt.getClaim(IdentityClaims.SCOPE) instanceof String scope) || scope.isBlank()) {
      return failure(IdentityClaims.SCOPE + " is not a space-separated string");
    }
    return OAuth2TokenValidatorResult.success();
  }

  private static OAuth2TokenValidatorResult failure(String description) {
    return OAuth2TokenValidatorResult.failure(
        new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, description, null));
  }

  private static boolean isUuid(String value) {
    if (value == null) {
      return false;
    }
    try {
      UUID.fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
