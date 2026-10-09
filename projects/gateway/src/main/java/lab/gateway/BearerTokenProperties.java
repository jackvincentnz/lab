package lab.gateway;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.Assert;

/**
 * Which bearer JWTs the gateway accepts from API clients. They are verified against their own key
 * pair, issuer, and audience rather than the identity token's, so an identity token minted for a
 * downstream service cannot be replayed at the gateway as a client credential.
 *
 * @param issuer the value every accepted token must carry in {@code iss}.
 * @param audience the value every accepted token must carry in {@code aud}.
 * @param publicKey the X.509 PEM RSA public key accepted tokens are signed with.
 * @param ephemeralKey whether to generate a key pair at startup when none is configured, and log
 *     its private half so tokens can be minted for it. That suits one local gateway and nothing
 *     else.
 */
@ConfigurationProperties("lab.gateway.bearer")
public record BearerTokenProperties(
    @DefaultValue("lab-bearer") String issuer,
    @DefaultValue("lab-gateway") String audience,
    Optional<String> publicKey,
    @DefaultValue("false") boolean ephemeralKey) {
  public BearerTokenProperties {
    Assert.hasText(issuer, "Bearer tokens need an issuer");
    Assert.hasText(audience, "Bearer tokens need an audience");
    Assert.isTrue(
        publicKey.isPresent() || ephemeralKey,
        "Configure lab.gateway.bearer.public-key, or lab.gateway.bearer.ephemeral-key for a local"
            + " gateway");
  }
}
