package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Base64;
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
  void from_withConfiguredPem_signsWithItUnderItsThumbprint() throws Exception {
    var pair = keyPair(2048);

    var key = SigningKey.from(properties(Optional.of(pem(pair)), false));

    assertThat(key.jwk().toRSAPrivateKey()).isEqualTo(pair.getPrivate());
    assertThat(key.jwk().toRSAPublicKey()).isEqualTo(pair.getPublic());
    assertThat(key.keyId())
        .isEqualTo(
            new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                .build()
                .computeThumbprint()
                .toString());
  }

  @Test
  void from_withTheSameConfiguredPem_namesTheKeyTheSameEverywhere() throws Exception {
    var pem = pem(keyPair(2048));

    var first = SigningKey.parse(pem);
    var second = SigningKey.parse(pem);

    assertThat(first.keyId()).isEqualTo(second.keyId());
  }

  @Test
  void from_prefersTheConfiguredPemOverAnEphemeralKey() throws Exception {
    var pair = keyPair(2048);

    var key = SigningKey.from(properties(Optional.of(pem(pair)), true));

    assertThat(key.jwk().toRSAPublicKey()).isEqualTo(pair.getPublic());
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
  void parse_withMalformedPem_failsWithoutEchoingIt() {
    var secret = randomString();

    assertThatThrownBy(() -> SigningKey.parse(secret))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContaining(secret);
  }

  @Test
  void parse_withPublicKeyPem_isRejected() throws Exception {
    var pair = keyPair(2048);
    var publicPem =
        "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(pair.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----\n";

    assertThatThrownBy(() -> SigningKey.parse(publicPem))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void parse_withShortKey_isRejected() throws Exception {
    var pem = pem(keyPair(1024));

    assertThatThrownBy(() -> SigningKey.parse(pem))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2048");
  }

  @Test
  void properties_withNeitherKeyNorEphemeralFlag_areRejected() {
    assertThatThrownBy(() -> properties(Optional.empty(), false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("private-key");
  }

  @Test
  void properties_withNonPositiveValidity_areRejected() {
    assertThatThrownBy(
            () -> new IdentityTokenProperties("lab-gateway", Duration.ZERO, Optional.empty(), true))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static IdentityTokenProperties properties(
      Optional<String> privateKey, boolean ephemeral) {
    return new IdentityTokenProperties("lab-gateway", Duration.ofMinutes(5), privateKey, ephemeral);
  }

  private static KeyPair keyPair(int bits) throws Exception {
    var generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(bits);
    return generator.generateKeyPair();
  }

  private static String pem(KeyPair pair) {
    return "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPrivate().getEncoded())
        + "\n-----END PRIVATE KEY-----\n";
  }
}
