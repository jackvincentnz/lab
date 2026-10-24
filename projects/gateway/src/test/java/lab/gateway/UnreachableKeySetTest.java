package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import lab.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Not a {@link GatewayTestSupport}: its property source would run after this one and restore the
 * reachable key set.
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "management.health.redis.enabled=false")
@Import(GatewayTestSupport.InMemorySessions.class)
class UnreachableKeySetTest extends TestBase {

  private static Downstream mopsService;

  @LocalServerPort int port;

  @BeforeAll
  static void startDownstream() {
    mopsService = Downstream.start("service");
  }

  @AfterAll
  static void stopDownstream() {
    mopsService.close();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    GatewayTestSupport.configureGateway(registry);
    registry.add(
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> "http://localhost:" + closedPort() + "/jwks");
    registry.add("lab.gateway.mops.service-uri", mopsService::uri);
  }

  @Test
  void bearerRequest_withUnreachableKeySet_isServerError() {
    var before = mopsService.requests();

    WebTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build()
        .get()
        .uri("/api/graphql")
        .header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + GatewayTestSupport.bearer(GatewayTestSupport.USER).mint())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(mopsService.requests()).isEqualTo(before);
  }

  private static int closedPort() {
    try (var socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
