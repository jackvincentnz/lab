package lab.gateway;

import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.Assert;

/**
 * How the gateway signs the identity token it hands to verticals.
 *
 * @param issuer the value every minted token carries in {@code iss}.
 * @param validFor how long a minted token is accepted after it is issued.
 * @param privateJwk the RSA private key to sign with, as a JWK carrying its {@code kid}. Every
 *     replica must hold the same key, since verticals fetch the JWK set from whichever replica
 *     answers.
 * @param ephemeralKey whether to generate a key at startup when none is configured. Tokens then
 *     survive only as long as the process, so this suits one local gateway and nothing else.
 */
@ConfigurationProperties("lab.gateway.token")
public record IdentityTokenProperties(
    @DefaultValue("lab-gateway") String issuer,
    @DefaultValue("PT5M") Duration validFor,
    Optional<String> privateJwk,
    @DefaultValue("false") boolean ephemeralKey) {
  public IdentityTokenProperties {
    Assert.hasText(issuer, "The identity token needs an issuer");
    Assert.isTrue(
        !validFor.isNegative() && !validFor.isZero(),
        "The identity token must be valid for a while");
    Assert.isTrue(
        privateJwk.isPresent() || ephemeralKey,
        "Configure lab.gateway.token.private-jwk, or lab.gateway.token.ephemeral-key for a local"
            + " gateway");
  }
}
