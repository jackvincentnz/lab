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
 * @param privateKey the PKCS#8 PEM RSA private key to sign with. Its {@code kid} is the public
 *     key's thumbprint, so every replica holding the same key publishes it under the same name.
 * @param ephemeralKey whether to generate a key at startup when none is configured. Tokens then
 *     survive only as long as the process, so this suits one local gateway and nothing else.
 */
@ConfigurationProperties("lab.gateway.token")
public record IdentityTokenProperties(
    @DefaultValue("lab-gateway") String issuer,
    @DefaultValue("PT5M") Duration validFor,
    Optional<String> privateKey,
    @DefaultValue("false") boolean ephemeralKey) {
  public IdentityTokenProperties {
    Assert.hasText(issuer, "The identity token needs an issuer");
    Assert.isTrue(
        !validFor.isNegative() && !validFor.isZero(),
        "The identity token must be valid for a while");
    Assert.isTrue(
        privateKey.isPresent() || ephemeralKey,
        "Configure lab.gateway.token.private-key, or lab.gateway.token.ephemeral-key for a local"
            + " gateway");
  }
}
