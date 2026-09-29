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
 * @param keyId the {@code kid} the signing key is published under.
 * @param privateKey the PKCS#8 PEM private key to sign with. Without one the gateway generates a
 *     key at startup, so tokens survive only as long as the process; verticals refetch the JWK set
 *     when a token names a key they do not hold.
 */
@ConfigurationProperties("lab.gateway.token")
public record IdentityTokenProperties(
    @DefaultValue("lab-gateway") String issuer,
    @DefaultValue("PT5M") Duration validFor,
    Optional<String> keyId,
    Optional<String> privateKey) {
  public IdentityTokenProperties {
    Assert.hasText(issuer, "The identity token needs an issuer");
    Assert.isTrue(
        !validFor.isNegative() && !validFor.isZero(),
        "The identity token must be valid for a while");
    Assert.isTrue(
        privateKey.isEmpty() || keyId.filter(id -> !id.isBlank()).isPresent(),
        "A configured signing key needs a key ID");
  }
}
