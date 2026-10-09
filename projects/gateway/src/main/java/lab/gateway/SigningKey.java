package lab.gateway;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.util.Assert;

/**
 * The RSA key pair the gateway signs identity tokens with. Its {@code kid} is the RFC 7638
 * thumbprint of the public key, so the name is the same on every replica that holds the key and
 * changes whenever the key does. Downstream services cache keys by {@code kid} and refetch only
 * when a token names one they do not hold, so a reused name would leave them verifying against a
 * stale key.
 */
public final class SigningKey {

  static final int MINIMUM_BITS = 2048;

  private final RSAKey key;

  private SigningKey(RSAKey key) {
    this.key = key;
  }

  /** The configured key, or a key generated for this process when the configuration allows. */
  public static SigningKey from(IdentityTokenProperties properties) {
    return properties.privateKey().map(SigningKey::parse).orElseGet(SigningKey::generate);
  }

  static SigningKey generate() {
    try {
      var generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(MINIMUM_BITS);
      var pair = generator.generateKeyPair();
      return of((RSAPublicKey) pair.getPublic(), pair.getPrivate());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Reads a PKCS#8 PEM private key and derives its public half. */
  static SigningKey parse(String pem) {
    PrivateKey privateKey;
    try {
      privateKey =
          RsaKeyConverters.pkcs8()
              .convert(new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
    } catch (RuntimeException e) {
      // The message stays generic because the value holds private key material.
      throw new IllegalArgumentException("The signing key is not a PKCS#8 PEM RSA private key");
    }
    Assert.isTrue(
        privateKey instanceof RSAPrivateCrtKey,
        "The signing key must carry its public exponent so the public key can be published");
    var crt = (RSAPrivateCrtKey) privateKey;
    Assert.isTrue(
        crt.getModulus().bitLength() >= MINIMUM_BITS, "The signing key must be at least 2048 bits");
    try {
      var publicKey =
          (RSAPublicKey)
              KeyFactory.getInstance("RSA")
                  .generatePublic(new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent()));
      return of(publicKey, crt);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException(e);
    }
  }

  private static SigningKey of(RSAPublicKey publicKey, PrivateKey privateKey) {
    try {
      return new SigningKey(
          new RSAKey.Builder(publicKey)
              .privateKey(privateKey)
              .keyUse(KeyUse.SIGNATURE)
              .algorithm(JWSAlgorithm.RS256)
              .keyIDFromThumbprint()
              .build());
    } catch (JOSEException e) {
      throw new IllegalStateException(e);
    }
  }

  public String keyId() {
    return key.getKeyID();
  }

  /** The private and public halves, for signing. */
  RSAKey jwk() {
    return key;
  }

  /** The public half as the JWK set document downstream services fetch. */
  public Map<String, Object> publicJwkSet() {
    return new JWKSet(key.toPublicJWK()).toJSONObject();
  }
}
