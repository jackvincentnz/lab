package lab.gateway;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lab.libs.identity.jwt.IdentityClaims;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** Mints the identity token a vertical receives in place of the browser's credentials. */
public final class IdentityTokenMinter {

  /** Form login is the only way to authenticate at the gateway today. */
  static final List<String> AUTHENTICATION_METHODS = List.of("form");

  private final IdentityTokenProperties properties;
  private final SigningKey key;
  private final JwtEncoder encoder;
  private final Clock clock;

  public IdentityTokenMinter(IdentityTokenProperties properties, SigningKey key, Clock clock) {
    this.properties = properties;
    this.key = key;
    this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key.jwk())));
    this.clock = clock;
  }

  /** A token for one forwarded request, addressed to the vertical named by {@code audience}. */
  public String mint(GatewayPrincipal principal, String sessionId, String audience) {
    var issuedAt = clock.instant();
    var claims =
        JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .audience(List.of(audience))
            .subject(principal.principal().toString())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(properties.validFor()))
            .id(UUID.randomUUID().toString())
            .claim(IdentityClaims.TENANT, principal.tenant().toString())
            .claim(IdentityClaims.SCOPE, String.join(" ", principal.scopes()))
            .claim(IdentityClaims.AUTHENTICATION_METHODS, AUTHENTICATION_METHODS)
            .claim(IdentityClaims.SESSION_ID, sessionId)
            .build();
    var header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(key.keyId()).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
