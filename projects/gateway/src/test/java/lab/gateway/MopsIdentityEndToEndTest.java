package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;

/** A real browser session in Redis, gateway signing/JWKS, and the real Mops HTTP server. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MopsIdentityEndToEndTest {
  static final GenericContainer<?> REDIS =
      new GenericContainer<>("redis:7.4-alpine").withExposedPorts(6379);
  static final int MOPS_PORT = unusedPort();

  static {
    REDIS.start();
  }

  @LocalServerPort int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    GatewayTestSupport.configureUsers(registry);
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    registry.add("lab.gateway.mops.service-uri", () -> "http://localhost:" + MOPS_PORT);
  }

  @Test
  void loggedInBrowser_reachesMopsUsingGatewayJwks() throws Exception {
    var executable =
        Path.of(
            System.getenv("TEST_SRCDIR"),
            System.getenv("TEST_WORKSPACE"),
            "projects/mops/service/src/main/main");
    var log = Files.createTempFile("mops-identity-e2e", ".log");
    var process =
        new ProcessBuilder(
                executable.toString(),
                "--server.port=" + MOPS_PORT,
                "--spring.profiles.active=test",
                "--mops.identity.development.enabled=false",
                "--mops.identity.jwk-set-uri=http://localhost:" + port + "/.well-known/jwks.json",
                "--spring.ai.google.genai.api-key=test-key",
                "--spring.ai.openai.api-key=test-key")
            .redirectErrorStream(true)
            .redirectOutput(log.toFile())
            .start();
    try {
      var direct =
          WebTestClient.bindToServer()
              .baseUrl("http://localhost:" + MOPS_PORT)
              .responseTimeout(Duration.ofSeconds(2))
              .build();
      var deadline = System.nanoTime() + Duration.ofSeconds(90).toNanos();
      boolean ready = false;
      while (process.isAlive() && System.nanoTime() < deadline) {
        try {
          direct.get().uri("/actuator/health").exchange().expectStatus().isOk();
          ready = true;
          break;
        } catch (Exception | AssertionError ignored) {
          Thread.sleep(200);
        }
      }
      assertThat(ready).withFailMessage("Mops did not start:\n%s", Files.readString(log)).isTrue();
      var query = "{\"query\":\"{ allBudgets { id } }\"}";
      direct
          .post()
          .uri("/graphql")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(query)
          .exchange()
          .expectStatus()
          .isUnauthorized();
      var client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
      client
          .post()
          .uri("/api/graphql")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(query)
          .exchange()
          .expectStatus()
          .isForbidden();
      var browser = new Browser(client).login();
      browser
          .authenticatedClient()
          .post()
          .uri("/api/graphql")
          .header("Authorization", "Bearer attacker", "Basic attacker")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(query)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.errors")
          .doesNotExist()
          .jsonPath("$.data.allBudgets")
          .isArray();
    } finally {
      process.destroy();
      if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
        process.destroyForcibly().waitFor();
      }
    }
  }

  private static int unusedPort() {
    try (var socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
