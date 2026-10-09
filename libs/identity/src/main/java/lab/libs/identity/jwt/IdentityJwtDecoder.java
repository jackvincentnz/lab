package lab.libs.identity.jwt;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/** Decodes identity tokens with the keys the trusted issuer publishes as a JWK set. */
public final class IdentityJwtDecoder {

  private IdentityJwtDecoder() {}

  /**
   * @param jwkSetUri where the issuer publishes its signing keys. Fetched on the first token and
   *     refreshed when a token names an unknown key, so the service starts without the issuer.
   * @param issuer the value every accepted token must carry in {@code iss}.
   * @param audience the name of this service, which every accepted token must be addressed to.
   */
  public static JwtDecoder create(String jwkSetUri, String issuer, String audience) {
    var decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(), new IdentityClaimsValidator(issuer, audience)));
    return decoder;
  }
}
