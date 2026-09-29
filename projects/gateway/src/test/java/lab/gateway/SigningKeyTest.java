package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class SigningKeyTest extends TestBase {

  @Test
  void from_withoutConfiguredKey_generatesOnePerProcess() throws Exception {
    var properties = properties(Optional.empty(), Optional.empty());

    var first = SigningKey.from(properties);
    var second = SigningKey.from(properties);

    assertThat(first.keyId()).isNotBlank().isNotEqualTo(second.keyId());
    assertThat(first.jwk().toRSAPublicKey()).isNotEqualTo(second.jwk().toRSAPublicKey());
    assertThat(first.jwk().isPrivate()).isTrue();
  }

  @Test
  void from_withConfiguredKey_signsWithItUnderTheConfiguredId() throws Exception {
    var keyId = randomString();
    var generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    var pair = generator.generateKeyPair();

    var key = SigningKey.from(properties(Optional.of(keyId), Optional.of(pem(pair))));

    assertThat(key.keyId()).isEqualTo(keyId);
    assertThat(key.jwk().toRSAPrivateKey()).isEqualTo(pair.getPrivate());
    assertThat(key.jwk().toRSAPublicKey()).isEqualTo(pair.getPublic());
  }

  @Test
  void publicJwkSet_exposesOnlyThePublicHalf() {
    var key = SigningKey.from(properties(Optional.empty(), Optional.empty()));

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
  void properties_withKeyButNoKeyId_areRejected() {
    assertThatThrownBy(() -> properties(Optional.empty(), Optional.of(randomString())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("key ID");
  }

  @Test
  void properties_withNonPositiveValidity_areRejected() {
    assertThatThrownBy(
            () ->
                new IdentityTokenProperties(
                    "lab-gateway", Duration.ZERO, Optional.empty(), Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static IdentityTokenProperties properties(
      Optional<String> keyId, Optional<String> privateKey) {
    return new IdentityTokenProperties("lab-gateway", Duration.ofMinutes(5), keyId, privateKey);
  }

  private static String pem(java.security.KeyPair pair) {
    return "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPrivate().getEncoded())
        + "\n-----END PRIVATE KEY-----\n";
  }
}
