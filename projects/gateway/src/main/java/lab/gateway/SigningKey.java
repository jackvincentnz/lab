package lab.gateway;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

/** The RSA key pair the gateway signs identity tokens with, published under one {@code kid}. */
public final class SigningKey {

  static final int MINIMUM_BITS = 2048;

  private final RSAKey key;

  private SigningKey(RSAKey key) {
    this.key = key;
  }

  /** The configured key, or a key generated for this process when the configuration allows. */
  public static SigningKey from(IdentityTokenProperties properties) {
    return properties.privateJwk().map(SigningKey::parse).orElseGet(SigningKey::generate);
  }

  /**
   * Generates a key under a fresh {@code kid}. Verticals cache keys by {@code kid} and refetch only
   * when a token names one they do not hold, so a reused name would leave them verifying against a
   * stale key until their cache expired.
   */
  static SigningKey generate() {
    try {
      var generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(MINIMUM_BITS);
      var pair = generator.generateKeyPair();
      return new SigningKey(
          new RSAKey.Builder((RSAPublicKey) pair.getPublic())
              .privateKey(pair.getPrivate())
              .keyID(UUID.randomUUID().toString())
              .keyUse(KeyUse.SIGNATURE)
              .algorithm(JWSAlgorithm.RS256)
              .build());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  static SigningKey parse(String jwk) {
    RSAKey key;
    try {
      key = RSAKey.parse(jwk);
    } catch (ParseException e) {
      // The message stays generic because the value holds private key material.
      throw new IllegalArgumentException("The signing key is not an RSA JWK");
    }
    if (!key.isPrivate()) {
      throw new IllegalArgumentException("The signing key JWK holds no private key");
    }
    if (key.size() < MINIMUM_BITS) {
      throw new IllegalArgumentException("The signing key must be at least 2048 bits");
    }
    if (key.getKeyID() == null || key.getKeyID().isBlank()) {
      throw new IllegalArgumentException("The signing key needs a kid");
    }
    if (key.getAlgorithm() != null && !JWSAlgorithm.RS256.equals(key.getAlgorithm())) {
      throw new IllegalArgumentException("The signing key must be for RS256");
    }
    if (key.getKeyUse() != null && !KeyUse.SIGNATURE.equals(key.getKeyUse())) {
      throw new IllegalArgumentException("The signing key must be for signatures");
    }
    return new SigningKey(key);
  }

  public String keyId() {
    return key.getKeyID();
  }

  /** The private and public halves, for signing. */
  RSAKey jwk() {
    return key;
  }

  /** The public half as the JWK set document verticals fetch. */
  public Map<String, Object> publicJwkSet() {
    return new JWKSet(key.toPublicJWK()).toJSONObject();
  }
}
