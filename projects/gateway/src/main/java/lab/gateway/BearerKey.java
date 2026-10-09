package lab.gateway;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.util.Assert;

/**
 * The RSA public key that client bearer JWTs are verified against. It is a separate key pair from
 * the {@link SigningKey}, so the gateway never holds what signs a client credential.
 */
public final class BearerKey {

  static final int MINIMUM_BITS = 2048;

  private final RSAPublicKey publicKey;
  private final Optional<String> generatedPrivateKey;

  private BearerKey(RSAPublicKey publicKey, Optional<String> generatedPrivateKey) {
    this.publicKey = publicKey;
    this.generatedPrivateKey = generatedPrivateKey;
  }

  /** The configured key, or a key pair generated for this process when the configuration allows. */
  public static BearerKey from(BearerTokenProperties properties) {
    return properties.publicKey().map(BearerKey::parse).orElseGet(BearerKey::generate);
  }

  static BearerKey generate() {
    try {
      var generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(MINIMUM_BITS);
      var pair = generator.generateKeyPair();
      return new BearerKey((RSAPublicKey) pair.getPublic(), Optional.of(pem(pair.getPrivate())));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Reads an X.509 PEM public key, the form {@code openssl pkey -pubout} writes. */
  static BearerKey parse(String pem) {
    RSAPublicKey key;
    try {
      key =
          RsaKeyConverters.x509()
              .convert(new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
    } catch (RuntimeException e) {
      // The message stays generic so a private key supplied here by mistake is never echoed.
      throw new IllegalArgumentException("The bearer key is not an X.509 PEM RSA public key");
    }
    Assert.notNull(key, "The bearer key is not an X.509 PEM RSA public key");
    Assert.isTrue(
        key.getModulus().bitLength() >= MINIMUM_BITS, "The bearer key must be at least 2048 bits");
    return new BearerKey(key, Optional.empty());
  }

  private static String pem(PrivateKey key) {
    return "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
            .encodeToString(key.getEncoded())
        + "\n-----END PRIVATE KEY-----\n";
  }

  public RSAPublicKey publicKey() {
    return publicKey;
  }

  /** The PKCS#8 PEM private half of a generated key, which nothing else can mint tokens for. */
  public Optional<String> generatedPrivateKey() {
    return generatedPrivateKey;
  }
}
