package lab.gateway;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.libs.identity.development.DevelopmentSigningKey;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/** Signs a fresh, five-minute identity token for each request forwarded to Mops. */
@Component
public final class DownstreamIdentity {
  private final RSAKey key;
  private final RSASSASigner signer;

  public DownstreamIdentity(Environment environment) {
    var configured = environment.getProperty("lab.gateway.identity.private-jwk");
    try {
      if (configured != null && !configured.isBlank()) {
        key = RSAKey.parse(configured);
      } else if (environment.acceptsProfiles(Profiles.of("local"))) {
        key =
            new RSAKey.Builder(DevelopmentSigningKey.publicKey())
                .privateKey(DevelopmentSigningKey.privateKey())
                .keyID(DevelopmentSigningKey.KEY_ID)
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();
      } else {
        throw new IllegalArgumentException(
            "lab.gateway.identity.private-jwk is required outside the local profile");
      }
      if (!key.isPrivate()
          || key.size() < 2048
          || key.getKeyID() == null
          || key.getKeyID().isBlank()
          || (key.getAlgorithm() != null && !JWSAlgorithm.RS256.equals(key.getAlgorithm()))
          || (key.getKeyUse() != null && !KeyUse.SIGNATURE.equals(key.getKeyUse()))) {
        throw new IllegalArgumentException(
            "Signing JWK must be a private RSA key of at least 2048 bits with a kid, for RS256"
                + " signatures");
      }
      signer = new RSASSASigner(key);
    } catch (ParseException | JOSEException e) {
      // Do not include parser details: configuration contains private key material.
      throw new IllegalArgumentException("Cannot load the gateway RSA signing JWK");
    }
  }

  public Map<String, Object> publicKeys() {
    return new JWKSet(key.toPublicJWK()).toJSONObject();
  }

  public String mint(GatewayPrincipal principal, String sessionId) {
    var now = Instant.now();
    var claims =
        new JWTClaimsSet.Builder()
            .issuer("lab-gateway")
            .audience("mops")
            .subject(principal.principal().toString())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(300)))
            .jwtID(UUID.randomUUID().toString())
            .claim("tenant", principal.tenant().toString())
            .claim("scope", String.join(" ", principal.scopes()))
            .claim("amr", List.of("form"))
            .claim("sid", sessionId)
            .build();
    var token =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
    try {
      token.sign(signer);
      return token.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Cannot sign downstream identity", e);
    }
  }
}
