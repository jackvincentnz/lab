package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import lab.libs.identity.testing.TestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DownstreamIdentityTest {
  private MockEnvironment configured() {
    return new MockEnvironment()
        .withProperty(
            "lab.gateway.identity.private-jwk",
            new RSAKey.Builder(TestTokens.devPublicKey())
                .privateKey(TestTokens.devPrivateKey())
                .keyID("configured-key")
                .build()
                .toJSONString());
  }

  @Test
  void configuredKey_signsTheSessionContract() throws Exception {
    var identity = new DownstreamIdentity(configured());
    var principal = new GatewayPrincipal(GatewayTestSupport.USER);
    var before = Instant.now().minusSeconds(1);
    var jwt = SignedJWT.parse(identity.mint(principal, "session-id"));
    var publicKey = JWKSet.parse(identity.publicKeys()).getKeyByKeyId("configured-key").toRSAKey();
    assertThat(publicKey.isPrivate()).isFalse();
    assertThat(jwt.verify(new RSASSAVerifier(publicKey))).isTrue();
    assertThat(jwt.getHeader().getKeyID()).isEqualTo("configured-key");
    var claims = jwt.getJWTClaimsSet();
    assertThat(claims.getIssuer()).isEqualTo("lab-gateway");
    assertThat(claims.getAudience()).containsExactly("mops");
    assertThat(claims.getSubject()).isEqualTo(principal.principal().toString());
    assertThat(claims.getStringClaim("tenant")).isEqualTo(principal.tenant().toString());
    assertThat(claims.getStringClaim("scope")).isEqualTo("mops:read mops:write");
    assertThat(claims.getStringListClaim("amr")).containsExactly("form");
    assertThat(claims.getStringClaim("sid")).isEqualTo("session-id");
    assertThat(claims.getIssueTime().toInstant()).isBetween(before, Instant.now());
    assertThat(claims.getExpirationTime().toInstant())
        .isEqualTo(claims.getIssueTime().toInstant().plusSeconds(300));
    assertThat(claims.getJWTID())
        .isNotBlank()
        .isNotEqualTo(
            SignedJWT.parse(identity.mint(principal, "session-id")).getJWTClaimsSet().getJWTID());
  }

  @Test
  void localProfile_usesTheSharedDevelopmentKey() throws Exception {
    var environment = new MockEnvironment();
    environment.setActiveProfiles("local");
    var identity = new DownstreamIdentity(environment);
    var jwt =
        SignedJWT.parse(identity.mint(new GatewayPrincipal(GatewayTestSupport.USER), "session"));
    assertThat(jwt.verify(new RSASSAVerifier(TestTokens.devPublicKey()))).isTrue();
    assertThat(jwt.getHeader().getKeyID()).isEqualTo(TestTokens.DEV_KEY_ID);
  }

  @Test
  void deploymentWithoutKey_failsClosed() {
    assertThatThrownBy(() -> new DownstreamIdentity(new MockEnvironment()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("required");
  }

  @Test
  void malformedKey_doesNotLeakConfiguration() {
    assertThatThrownBy(
            () ->
                new DownstreamIdentity(
                    new MockEnvironment()
                        .withProperty("lab.gateway.identity.private-jwk", "private-secret")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContaining("private-secret");
  }

  @Test
  void publicOnlyKey_isRejected() {
    var environment =
        configured()
            .withProperty(
                "lab.gateway.identity.private-jwk",
                new RSAKey.Builder(TestTokens.devPublicKey())
                    .keyID("public-only")
                    .build()
                    .toJSONString());
    assertThatThrownBy(() -> new DownstreamIdentity(environment))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void keyWithoutId_isRejected() {
    var environment =
        configured()
            .withProperty(
                "lab.gateway.identity.private-jwk",
                new RSAKey.Builder(TestTokens.devPublicKey())
                    .privateKey(TestTokens.devPrivateKey())
                    .build()
                    .toJSONString());
    assertThatThrownBy(() -> new DownstreamIdentity(environment))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
