package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Base64;
import java.util.Optional;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.security.converter.RsaKeyConverters;

class BearerKeyTest extends TestBase {

  @Test
  void from_withConfiguredPem_verifiesWithIt() throws Exception {
    var pair = keyPair(2048);

    var key = BearerKey.from(properties(Optional.of(publicPem(pair)), false));

    assertThat(key.publicKey()).isEqualTo(pair.getPublic());
    assertThat(key.generatedPrivateKey()).isEmpty();
  }

  @Test
  void from_prefersTheConfiguredPemOverAnEphemeralKey() throws Exception {
    var pair = keyPair(2048);

    var key = BearerKey.from(properties(Optional.of(publicPem(pair)), true));

    assertThat(key.publicKey()).isEqualTo(pair.getPublic());
    assertThat(key.generatedPrivateKey()).isEmpty();
  }

  @Test
  void from_withEphemeralKey_generatesAPairAndExposesItsPrivateHalf() {
    var properties = properties(Optional.empty(), true);

    var first = BearerKey.from(properties);
    var second = BearerKey.from(properties);

    assertThat(first.publicKey()).isNotEqualTo(second.publicKey());
    var privateKey =
        (RSAPrivateCrtKey)
            RsaKeyConverters.pkcs8()
                .convert(
                    new ByteArrayInputStream(
                        first
                            .generatedPrivateKey()
                            .orElseThrow()
                            .getBytes(StandardCharsets.UTF_8)));
    assertThat(privateKey.getModulus()).isEqualTo(first.publicKey().getModulus());
  }

  @Test
  void parse_withMalformedPem_failsWithoutEchoingIt() {
    var secret = randomString();

    assertThatThrownBy(() -> BearerKey.parse(secret))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContaining(secret);
  }

  @Test
  void parse_withPrivateKeyPem_isRejectedWithoutEchoingIt() throws Exception {
    var pair = keyPair(2048);
    var encoded = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
    var privatePem =
        "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(pair.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----\n";

    assertThatThrownBy(() -> BearerKey.parse(privatePem))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContaining(encoded.substring(0, 64));
  }

  @Test
  void parse_withShortKey_isRejected() throws Exception {
    var pem = publicPem(keyPair(1024));

    assertThatThrownBy(() -> BearerKey.parse(pem))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2048");
  }

  @Test
  void properties_withNeitherKeyNorEphemeralFlag_areRejected() {
    assertThatThrownBy(() -> properties(Optional.empty(), false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("public-key");
  }

  @Test
  void properties_withBlankIssuer_areRejected() {
    assertThatThrownBy(() -> new BearerTokenProperties(" ", randomString(), Optional.empty(), true))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void properties_withBlankAudience_areRejected() {
    assertThatThrownBy(() -> new BearerTokenProperties(randomString(), " ", Optional.empty(), true))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private BearerTokenProperties properties(Optional<String> publicKey, boolean ephemeral) {
    return new BearerTokenProperties(randomString(), randomString(), publicKey, ephemeral);
  }

  private static KeyPair keyPair(int bits) throws Exception {
    var generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(bits);
    return generator.generateKeyPair();
  }

  private static String publicPem(KeyPair pair) {
    return "-----BEGIN PUBLIC KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPublic().getEncoded())
        + "\n-----END PUBLIC KEY-----\n";
  }
}
