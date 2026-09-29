package lab.libs.identity.development;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/** Ephemeral signing key shared by local development and test support within one JVM. */
public final class DevelopmentSigningKey {
  public static final String KEY_ID = "dev";
  private static final KeyPair KEY_PAIR = generateKeyPair();

  private DevelopmentSigningKey() {}

  public static RSAPublicKey publicKey() {
    return (RSAPublicKey) KEY_PAIR.getPublic();
  }

  public static RSAPrivateKey privateKey() {
    return (RSAPrivateKey) KEY_PAIR.getPrivate();
  }

  private static KeyPair generateKeyPair() {
    try {
      var generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
