package lab.gateway;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.converter.RsaKeyConverters;

/** The RSA key pair the gateway signs identity tokens with, published under one {@code kid}. */
public final class SigningKey {

  private final RSAKey key;

  private SigningKey(RSAKey key) {
    this.key = key;
  }

  /** The configured key, or a key generated for this process when none is configured. */
  public static SigningKey from(IdentityTokenProperties properties) {
    return properties
        .privateKey()
        .map(pem -> of(properties.keyId().orElseThrow(), parse(pem)))
        .orElseGet(SigningKey::generate);
  }

  static SigningKey generate() {
    try {
      var generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      var pair = generator.generateKeyPair();
      return new SigningKey(
          builder((RSAPublicKey) pair.getPublic(), UUID.randomUUID().toString())
              .privateKey(pair.getPrivate())
              .build());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  static SigningKey of(String keyId, RSAPrivateKey privateKey) {
    if (!(privateKey instanceof RSAPrivateCrtKey crt)) {
      throw new IllegalArgumentException("The signing key must carry its public exponent");
    }
    try {
      var publicKey =
          (RSAPublicKey)
              KeyFactory.getInstance("RSA")
                  .generatePublic(new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent()));
      return new SigningKey(builder(publicKey, keyId).privateKey(privateKey).build());
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException(e);
    }
  }

  private static RSAPrivateKey parse(String pem) {
    return RsaKeyConverters.pkcs8()
        .convert(new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
  }

  private static RSAKey.Builder builder(RSAPublicKey publicKey, String keyId) {
    return new RSAKey.Builder(publicKey)
        .keyID(keyId)
        .keyUse(KeyUse.SIGNATURE)
        .algorithm(JWSAlgorithm.RS256);
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
