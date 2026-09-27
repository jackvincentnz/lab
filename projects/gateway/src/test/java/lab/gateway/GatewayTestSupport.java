package lab.gateway;

import java.util.concurrent.ConcurrentHashMap;
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
abstract class GatewayTestSupport {
  static final String PRINCIPAL = "11111111-1111-1111-1111-111111111111";
  static final String TENANT = "22222222-2222-2222-2222-222222222222";
  static final String PASSWORD = "{bcrypt}" + new BCryptPasswordEncoder(4).encode("password");

  @LocalServerPort int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    configureUsers(registry);
  }

  static void configureUsers(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.users[0].username", () -> "alice");
    registry.add("lab.gateway.users[0].password", () -> PASSWORD);
    registry.add("lab.gateway.users[0].principal", () -> PRINCIPAL);
    registry.add("lab.gateway.users[0].tenant", () -> TENANT);
    registry.add("lab.gateway.users[0].scopes[0]", () -> "mops:read");
    registry.add("lab.gateway.users[0].scopes[1]", () -> "mops:write");
  }

  WebTestClient client() {
    return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  Browser browser() {
    return new Browser(client());
  }

  @TestConfiguration(proxyBeanMethods = false)
  @EnableSpringWebSession
  static class InMemorySessions {
    @Bean
    ReactiveSessionRepository<?> sessions() {
      return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
    }
  }
}
