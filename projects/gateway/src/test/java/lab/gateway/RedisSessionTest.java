package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.RequiresDocker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;

/** Covers what only a real Redis shows: the session is stored there and logout deletes it. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RequiresDocker
class RedisSessionTest {
  static final GenericContainer<?> REDIS =
      new GenericContainer<>(
              "redis:8.12.0-alpine@sha256:5af80107c1eeecb484a2269b80a26d12bfa6521787da4663bd9f19f60a163483")
          .withExposedPorts(6379);

  static {
    REDIS.start();
  }

  @LocalServerPort int port;
  @Autowired ReactiveSessionRepository<? extends Session> sessions;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    GatewayTestSupport.configureGateway(registry);
  }

  @Test
  void login_storesSessionInRedis() {
    var browser = browser().login();

    assertThat(sessions.findById(browser.anonymousSession).block()).isNull();
    assertThat(sessions.findById(browser.session).block()).isNotNull();
  }

  @Test
  void logout_deletesSessionFromRedis() {
    var browser = browser().login();

    browser.authenticatedClient().post().uri("/logout").exchange().expectStatus().isFound();

    assertThat(sessions.findById(browser.session).block()).isNull();
  }

  private Browser browser() {
    return new Browser(WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build());
  }
}
