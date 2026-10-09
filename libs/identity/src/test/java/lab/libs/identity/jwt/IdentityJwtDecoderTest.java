package lab.libs.identity.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import lab.libs.identity.testing.JwkSetServer;
import lab.libs.identity.testing.TestTokens;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class IdentityJwtDecoderTest {

  private static JwkSetServer jwkSet;
  private static JwtDecoder decoder;

  @BeforeAll
  static void start() {
    jwkSet = JwkSetServer.start(TestTokens.devJwkSet());
    decoder = IdentityJwtDecoder.create(jwkSet.uri(), TestTokens.DEFAULT_ISSUER, "mops");
  }

  @AfterAll
  static void stop() {
    jwkSet.close();
  }

  @Test
  void decode_acceptsTokenSignedWithAPublishedKey() {
    var principalId = UUID.randomUUID();
    var tenantId = UUID.randomUUID();
    var token =
        TestTokens.forAudience("mops")
            .principal(principalId)
            .tenant(tenantId)
            .scopes("mops:read")
            .mint();

    var jwt = decoder.decode(token);

    assertThat(IdentityClaims.identity(jwt).principalId()).isEqualTo(principalId);
    assertThat(IdentityClaims.identity(jwt).tenantId()).isEqualTo(tenantId);
    assertThat(IdentityClaims.identity(jwt).scopes()).containsExactly("mops:read");
    assertThat(jwt.getClaimAsStringList(IdentityClaims.AUTHENTICATION_METHODS))
        .containsExactly("form");
  }

  @Test
  void decode_rejectsTokenSignedWithAnotherKey() {
    var token = TestTokens.forAudience("mops").signedWith(TestTokens.foreignPrivateKey()).mint();

    assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void decode_rejectsTokenNamingAnUnpublishedKey() {
    var token = TestTokens.forAudience("mops").keyId("retired").mint();

    assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void decode_rejectsExpiredToken() {
    var token = TestTokens.forAudience("mops").expired().mint();

    assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void decode_rejectsTokenForAnotherVertical() {
    var token = TestTokens.forAudience("organizer").mint();

    assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void decode_rejectsTokenWithoutExpiry() {
    var token = TestTokens.forAudience("mops").withoutExpiry().mint();

    assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
  }

  @Test
  void decode_rejectsTokenFromAnotherIssuer() {
    var token = TestTokens.forAudience("mops").issuer("someone").mint();

    assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
  }
}
