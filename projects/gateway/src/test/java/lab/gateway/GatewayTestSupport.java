package lab.gateway;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lab.libs.identity.testing.JwkSetServer;
import lab.libs.identity.testing.TestTokens;
import lab.test.TestBase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

  static final String BEARER_ISSUER = "https://issuer.lab.test";
  static final String BEARER_AUDIENCE = "lab-gateway";

  /** Publishes the keys bearer tokens are signed with, as the configured issuer does. */
  static final JwkSetServer BEARER_KEYS = JwkSetServer.start(TestTokens.devJwkSet());

  @LocalServerPort int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    configureGateway(registry);
  }

  static void configureGateway(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.token.ephemeral-key", () -> true);
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> BEARER_ISSUER);
    registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", BEARER_KEYS::uri);
    registry.add("lab.gateway.users[0].username", USER::username);
    registry.add("lab.gateway.users[0].password", USER::password);
    registry.add("lab.gateway.users[0].principal", USER::principal);
    registry.add("lab.gateway.users[0].tenant", USER::tenant);
    registry.add("lab.gateway.users[0].scopes[0]", () -> USER.scopes().get(0));
    registry.add("lab.gateway.users[0].scopes[1]", () -> USER.scopes().get(1));
  }

  /** A bearer token from the configured issuer for the given user, addressed to the gateway. */
  static TestTokens.Token bearer(GatewayUsers.ConfiguredUser user) {
    return TestTokens.forAudience(BEARER_AUDIENCE)
        .issuer(BEARER_ISSUER)
        .principal(user.principal())
        .tenant(user.tenant())
        .scopes(user.scopes().toArray(String[]::new));
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
