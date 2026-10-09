package lab.gateway;

import lab.libs.identity.jwt.IdentityClaimsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class BearerTokenConfiguration {
  private static final Logger log = LoggerFactory.getLogger(BearerTokenConfiguration.class);

  @Bean
  BearerKey bearerKey(BearerTokenProperties properties) {
    var key = BearerKey.from(properties);
    key.generatedPrivateKey()
        .ifPresent(
            privateKey ->
                log.warn(
                    "Generated a throwaway bearer key for this process. Mint local bearer tokens"
                        + " with this private key, see projects/gateway/docs/bearer-tokens.md:\n{}",
                    privateKey));
    return key;
  }

  /**
   * Bearer tokens carry the same claims as the identity token, so they are checked by the same
   * validator, against the bearer issuer and the gateway as audience.
   */
  @Bean
  ReactiveJwtDecoder bearerTokenDecoder(BearerTokenProperties properties, BearerKey key) {
    var decoder = NimbusReactiveJwtDecoder.withPublicKey(key.publicKey()).build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(),
            new IdentityClaimsValidator(properties.issuer(), properties.audience())));
    return decoder;
  }
}
