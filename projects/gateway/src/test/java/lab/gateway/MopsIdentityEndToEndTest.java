package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
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

  static {
    REDIS.start();
  }

  @LocalServerPort int port;
  @Autowired GatewayProperties gatewayProperties;
  @Autowired CachingRouteLocator routes;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    GatewayTestSupport.configureUsers(registry);
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
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
                "--server.port=0",
                "--spring.profiles.active=test",
                "--mops.identity.development.enabled=false",
                "--mops.identity.jwk-set-uri=http://localhost:" + port + "/.well-known/jwks.json",
                "--spring.ai.google.genai.api-key=test-key",
                "--spring.ai.openai.api-key=test-key")
            .redirectErrorStream(true)
            .redirectOutput(log.toFile())
            .start();
    try {
      var deadline = System.nanoTime() + Duration.ofSeconds(90).toNanos();
      var boundPort = Pattern.compile("Tomcat started on port (\\d+)");
      int mopsPort = 0;
      while (process.isAlive() && System.nanoTime() < deadline) {
        var matcher = boundPort.matcher(Files.readString(log));
        if (matcher.find()) {
          mopsPort = Integer.parseInt(matcher.group(1));
          break;
        }
        Thread.sleep(200);
      }
      assertThat(mopsPort)
          .withFailMessage("Mops did not start:\n%s", Files.readString(log))
          .isPositive();
      var mopsUri = URI.create("http://localhost:" + mopsPort);
      // Keep the production route's predicates and filters; only its destination changes.
      gatewayProperties.getRoutes().stream()
          .filter(route -> route.getId().equals("mops-api"))
          .findFirst()
          .orElseThrow()
          .setUri(mopsUri);
      routes.refresh().blockLast(Duration.ofSeconds(10));
      var direct =
          WebTestClient.bindToServer()
              .baseUrl(mopsUri.toString())
              .responseTimeout(Duration.ofSeconds(10))
              .build();
      direct.get().uri("/actuator/health").exchange().expectStatus().isOk();
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
}
