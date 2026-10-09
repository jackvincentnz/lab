package lab.gateway;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lab.libs.identity.jwt.IdentityClaims;
import lab.test.TestBase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Boots the gateway with in-memory sessions so tests need neither Docker nor Redis. */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "management.health.redis.enabled=false")
@Import(GatewayTestSupport.InMemorySessions.class)
abstract class GatewayTestSupport extends TestBase {
  /** The one user the shared Spring context is configured with. */
  static final String PASSWORD = "password";

  static final GatewayUsers.ConfiguredUser USER =
      new GatewayUsers.ConfiguredUser(
          "alice",
          "{bcrypt}" + new BCryptPasswordEncoder(4).encode(PASSWORD),
          UUID.randomUUID(),
          UUID.randomUUID(),
          List.of("mops:read", "mops:write"));

  /** The key pair client bearer tokens are signed with; the gateway holds only its public half. */
  static final KeyPair BEARER_KEY = keyPair();

  @LocalServerPort int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    configureGateway(registry);
  }

  static void configureGateway(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.token.ephemeral-key", () -> true);
    registry.add("lab.gateway.bearer.public-key", () -> publicKeyPem(BEARER_KEY));
    registry.add("lab.gateway.users[0].username", USER::username);
    registry.add("lab.gateway.users[0].password", USER::password);
    registry.add("lab.gateway.users[0].principal", USER::principal);
    registry.add("lab.gateway.users[0].tenant", USER::tenant);
    registry.add("lab.gateway.users[0].scopes[0]", () -> USER.scopes().get(0));
    registry.add("lab.gateway.users[0].scopes[1]", () -> USER.scopes().get(1));
  }

  static KeyPair keyPair() {
    try {
      var generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  static String publicKeyPem(KeyPair pair) {
    return "-----BEGIN PUBLIC KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(pair.getPublic().getEncoded())
        + "\n-----END PUBLIC KEY-----\n";
  }

  /** Claims of a client bearer token the gateway accepts, for the given user. */
  static JwtClaimsSet.Builder bearerClaims(GatewayUsers.ConfiguredUser user) {
    var issuedAt = Instant.now();
    return JwtClaimsSet.builder()
        .issuer("lab-bearer")
        .audience(List.of("lab-gateway"))
        .subject(user.principal().toString())
        .issuedAt(issuedAt)
        .expiresAt(issuedAt.plus(Duration.ofMinutes(5)))
        .claim(IdentityClaims.TENANT, user.tenant().toString())
        .claim(IdentityClaims.SCOPE, String.join(" ", user.scopes()));
  }

  static String signBearer(JwtClaimsSet claims, KeyPair key) {
    var jwk =
        new RSAKey.Builder((RSAPublicKey) key.getPublic()).privateKey(key.getPrivate()).build();
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)))
        .encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
        .getTokenValue();
  }

  WebTestClient client() {
    return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  Browser browser() {
    return new Browser(client());
  }

  @TestConfiguration
  @EnableSpringWebSession
  static class InMemorySessions {
    @Bean
    ReactiveSessionRepository<?> sessions() {
      return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
    }
  }
}
