package lab.libs.identity.testing;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lab.libs.identity.jwt.IdentityClaims;

/** Mints identity tokens for tests, signed with a key pair generated per test run. */
public final class TestTokens {

  /** The {@code iss} tokens carry unless a test names another issuer. */
  public static final String DEFAULT_ISSUER = "lab-gateway";

  /** The {@code kid} the development key is published under. */
  public static final String DEV_KEY_ID = "dev";

  private static final KeyPair DEV_KEY_PAIR = generateKeyPair();

  private TestTokens() {}

  public static RSAPublicKey devPublicKey() {
    return (RSAPublicKey) DEV_KEY_PAIR.getPublic();
  }

  public static RSAPrivateKey devPrivateKey() {
    return (RSAPrivateKey) DEV_KEY_PAIR.getPrivate();
  }

  /** The development public key as the JWK set JSON an issuer would publish. */
  public static String devJwkSet() {
    var key =
        new RSAKey.Builder(devPublicKey()).keyID(DEV_KEY_ID).algorithm(JWSAlgorithm.RS256).build();
    return new JWKSet(key).toString();
  }

  /** A key the issuer never signed with, for asserting that foreign signatures are rejected. */
  public static RSAPrivateKey foreignPrivateKey() {
    return (RSAPrivateKey) generateKeyPair().getPrivate();
  }

  /** Starts a token addressed to one vertical, valid for five minutes from now. */
  public static Token forAudience(String audience) {
    return new Token(audience);
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

  public static final class Token {

    private final String audience;
    private String issuer = DEFAULT_ISSUER;
    private UUID principalId = UUID.randomUUID();
    private UUID tenantId = UUID.randomUUID();
    private Set<String> scopes;
    private List<String> authenticationMethods = List.of("form");
    private String sessionId = UUID.randomUUID().toString();
    private Instant issuedAt = Instant.now();
    private Duration validFor = Duration.ofMinutes(5);
    private String keyId = DEV_KEY_ID;
    private RSAPrivateKey signingKey = devPrivateKey();

    private Token(String audience) {
      this.audience = Objects.requireNonNull(audience, "audience");
      this.scopes = Set.of(audience + ":read", audience + ":write");
    }

    public Token issuer(String issuer) {
      this.issuer = issuer;
      return this;
    }

    public Token principal(UUID principalId) {
      this.principalId = principalId;
      return this;
    }

    public Token tenant(UUID tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Token scopes(String... scopes) {
      this.scopes = Set.of(scopes);
      return this;
    }

    public Token authenticationMethods(String... methods) {
      this.authenticationMethods = List.of(methods);
      return this;
    }

    public Token sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Token issuedAt(Instant issuedAt) {
      this.issuedAt = issuedAt;
      return this;
    }

    public Token validFor(Duration validFor) {
      this.validFor = validFor;
      return this;
    }

    /** Issued ten minutes ago and valid for five, so it is expired beyond any clock skew. */
    public Token expired() {
      return issuedAt(Instant.now().minus(Duration.ofMinutes(10))).validFor(Duration.ofMinutes(5));
    }

    /** Omits {@code exp}, which the contract requires. */
    public Token withoutExpiry() {
      this.validFor = null;
      return this;
    }

    /** Names a key in the token header; the published set must hold it for verification. */
    public Token keyId(String keyId) {
      this.keyId = keyId;
      return this;
    }

    public Token signedWith(RSAPrivateKey signingKey) {
      this.signingKey = signingKey;
      return this;
    }

    public String mint() {
      var claims =
          new JWTClaimsSet.Builder()
              .issuer(issuer)
              .audience(audience)
              .subject(principalId.toString())
              .issueTime(Date.from(issuedAt))
              .expirationTime(validFor == null ? null : Date.from(issuedAt.plus(validFor)))
              .jwtID(UUID.randomUUID().toString())
              .claim(IdentityClaims.TENANT, tenantId.toString())
              .claim(IdentityClaims.SCOPE, String.join(" ", scopes))
              .claim(IdentityClaims.AUTHENTICATION_METHODS, authenticationMethods)
              .claim(IdentityClaims.SESSION_ID, sessionId)
              .build();
      var header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build();
      var jwt = new SignedJWT(header, claims);
      try {
        jwt.sign(new RSASSASigner(signingKey));
      } catch (JOSEException e) {
        throw new IllegalStateException(e);
      }
      return jwt.serialize();
    }
  }
}
