package lab.libs.identity.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

class IdentityClaimsValidatorTest {

  private static final String ISSUER = "issuer";

  private final IdentityClaimsValidator validator = new IdentityClaimsValidator(ISSUER, "mops");

  @Test
  void validate_acceptsACompleteContractToken() {
    assertThat(validator.validate(contractToken()).hasErrors()).isFalse();
  }

  @Test
  void validate_fails_whenIssuerIsNotTrusted() {
    var result = validator.validate(contractToken(claims -> claims.claim(JwtClaimNames.ISS, "x")));

    assertFailure(result, "iss");
  }

  @Test
  void validate_fails_whenAudienceIsAnotherService() {
    var result = validator.validate(contractToken(claims -> claims.audience(List.of("organizer"))));

    assertFailure(result, "aud");
  }

  @Test
  void validate_fails_whenExpiryIsMissing() {
    var result = validator.validate(contractToken(claims -> claims.expiresAt(null)));

    assertFailure(result, "exp");
  }

  @Test
  void validate_fails_whenSubjectIsNotAUuid() {
    var result = validator.validate(contractToken(claims -> claims.subject("alice")));

    assertFailure(result, "sub");
  }

  @Test
  void validate_fails_whenTenantIsMissing() {
    var result =
        validator.validate(contractToken(claims -> claims.claim(IdentityClaims.TENANT, null)));

    assertFailure(result, "tenant");
  }

  @Test
  void validate_fails_whenScopeIsNotAString() {
    var result =
        validator.validate(
            contractToken(claims -> claims.claim(IdentityClaims.SCOPE, List.of("mops:read"))));

    assertFailure(result, "scope");
  }

  private static void assertFailure(OAuth2TokenValidatorResult result, String claim) {
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getErrors())
        .singleElement()
        .extracting("description")
        .asString()
        .contains(claim);
  }

  /** A token carrying every claim the contract requires, as the trusted issuer would mint it. */
  private static Jwt contractToken() {
    return contractToken(claims -> {});
  }

  private static Jwt contractToken(Consumer<Jwt.Builder> customizer) {
    var builder =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim(JwtClaimNames.ISS, ISSUER)
            .audience(List.of("mops"))
            .subject(UUID.randomUUID().toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .claim(IdentityClaims.TENANT, UUID.randomUUID().toString())
            .claim(IdentityClaims.SCOPE, "mops:read mops:write");
    customizer.accept(builder);
    return builder.build();
  }
}
