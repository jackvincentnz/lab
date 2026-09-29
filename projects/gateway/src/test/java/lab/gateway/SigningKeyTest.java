package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class SigningKeyTest extends TestBase {

  @Test
  void from_withEphemeralKey_generatesADistinctKeyEachTime() throws Exception {
    var properties = properties(Optional.empty(), true);

    var first = SigningKey.from(properties);
    var second = SigningKey.from(properties);

    assertThat(first.keyId()).isNotBlank().isNotEqualTo(second.keyId());
    assertThat(first.jwk().toRSAPublicKey()).isNotEqualTo(second.jwk().toRSAPublicKey());
    assertThat(first.jwk().isPrivate()).isTrue();
  }

  @Test
  void from_withConfiguredJwk_signsWithItUnderItsId() throws Exception {
    var keyId = randomString();
    var configured = new RSAKeyGenerator(2048).keyID(keyId).generate();

    var key = SigningKey.from(properties(Optional.of(configured.toJSONString()), false));

    assertThat(key.keyId()).isEqualTo(keyId);
    assertThat(key.jwk().toRSAPrivateKey()).isEqualTo(configured.toRSAPrivateKey());
    assertThat(key.jwk().toRSAPublicKey()).isEqualTo(configured.toRSAPublicKey());
  }

  @Test
  void from_prefersTheConfiguredJwkOverAnEphemeralKey() throws Exception {
    var configured = new RSAKeyGenerator(2048).keyID(randomString()).generate();

    var key = SigningKey.from(properties(Optional.of(configured.toJSONString()), true));

    assertThat(key.keyId()).isEqualTo(configured.getKeyID());
  }

  @Test
  void publicJwkSet_exposesOnlyThePublicHalf() {
    var key = SigningKey.from(properties(Optional.empty(), true));

    var set = key.publicJwkSet();

    @SuppressWarnings("unchecked")
    var keys = (List<Map<String, Object>>) set.get("keys");
    assertThat(keys).hasSize(1);
    assertThat(keys.get(0))
        .containsEntry("kid", key.keyId())
        .containsEntry("kty", "RSA")
        .containsEntry("use", "sig")
        .containsEntry("alg", "RS256")
        .containsKeys("n", "e")
        .doesNotContainKeys("d", "p", "q");
  }

  @Test
  void parse_withMalformedJwk_failsWithoutEchoingIt() {
    var secret = randomString();

    assertThatThrownBy(() -> SigningKey.parse(secret))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContaining(secret);
  }

  @Test
  void parse_withPublicOnlyJwk_isRejected() throws Exception {
    var jwk = new RSAKeyGenerator(2048).keyID(randomString()).generate().toPublicJWK();

    assertThatThrownBy(() -> SigningKey.parse(jwk.toJSONString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("private");
  }

  @Test
  void parse_withoutKid_isRejected() throws Exception {
    var jwk = new RSAKeyGenerator(2048).generate();

    assertThatThrownBy(() -> SigningKey.parse(jwk.toJSONString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("kid");
  }

  @Test
  void parse_withShortKey_isRejected() throws Exception {
    var generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(1024);
    var pair = generator.generateKeyPair();
    var jwk =
        new RSAKey.Builder((RSAPublicKey) pair.getPublic())
            .privateKey(pair.getPrivate())
            .keyID(randomString())
            .build();

    assertThatThrownBy(() -> SigningKey.parse(jwk.toJSONString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2048");
  }

  @Test
  void parse_withKeyForAnotherAlgorithmOrUse_isRejected() throws Exception {
    var otherAlgorithm =
        new RSAKeyGenerator(2048).keyID(randomString()).algorithm(JWSAlgorithm.RS512).generate();
    var otherUse =
        new RSAKeyGenerator(2048).keyID(randomString()).keyUse(KeyUse.ENCRYPTION).generate();

    assertThatThrownBy(() -> SigningKey.parse(otherAlgorithm.toJSONString()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SigningKey.parse(otherUse.toJSONString()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void properties_withNeitherKeyNorEphemeralFlag_areRejected() {
    assertThatThrownBy(() -> properties(Optional.empty(), false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("private-jwk");
  }

  @Test
  void properties_withNonPositiveValidity_areRejected() {
    assertThatThrownBy(
            () -> new IdentityTokenProperties("lab-gateway", Duration.ZERO, Optional.empty(), true))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static IdentityTokenProperties properties(
      Optional<String> privateJwk, boolean ephemeral) {
    return new IdentityTokenProperties("lab-gateway", Duration.ofMinutes(5), privateJwk, ephemeral);
  }
}
