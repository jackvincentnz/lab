package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class IdentityTokenMinterTest extends TestBase {

  private final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
  private final Duration validFor = Duration.ofMinutes(ThreadLocalRandom.current().nextInt(1, 60));
  private final IdentityTokenProperties properties =
      new IdentityTokenProperties(randomString(), validFor, Optional.empty(), true);
  private final SigningKey key = SigningKey.generate();
  private final IdentityTokenMinter minter =
      new IdentityTokenMinter(properties, key, fixedClock(now));

  private final Caller caller =
      new Caller(
          UUID.randomUUID(), UUID.randomUUID(), List.of("mops:read", "mops:write"), randomString());
  private final String audience = randomString();

  @Test
  void mint_signsWithTheGatewayKeyNamedByKid() throws Exception {
    var jwt = SignedJWT.parse(minter.mint(caller, audience));

    assertThat(jwt.getHeader().getKeyID()).isEqualTo(key.keyId());
    assertThat(jwt.getHeader().getAlgorithm().getName()).isEqualTo("RS256");
    assertThat(jwt.verify(new RSASSAVerifier(key.jwk().toRSAPublicKey()))).isTrue();
  }

  @Test
  void mint_carriesTheIdentityContract() throws Exception {
    var claims = SignedJWT.parse(minter.mint(caller, audience)).getJWTClaimsSet();

    assertThat(claims.getIssuer()).isEqualTo(properties.issuer());
    assertThat(claims.getAudience()).containsExactly(audience);
    assertThat(claims.getSubject()).isEqualTo(caller.principal().toString());
    assertThat(claims.getIssueTime()).isEqualTo(Date.from(now));
    assertThat(claims.getExpirationTime()).isEqualTo(Date.from(now.plus(validFor)));
    assertThat(claims.getStringClaim("tenant")).isEqualTo(caller.tenant().toString());
    assertThat(claims.getStringClaim("scope")).isEqualTo("mops:read mops:write");
    assertThat(claims.getStringListClaim("amr")).containsExactly(caller.authenticationMethod());
  }

  @Test
  void mint_givesEveryTokenItsOwnId() throws Exception {
    var first = SignedJWT.parse(minter.mint(caller, audience)).getJWTClaimsSet();
    var second = SignedJWT.parse(minter.mint(caller, audience)).getJWTClaimsSet();

    assertThat(first.getJWTID()).isNotBlank().isNotEqualTo(second.getJWTID());
  }
}
