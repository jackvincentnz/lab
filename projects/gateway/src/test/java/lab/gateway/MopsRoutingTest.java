package lab.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/** Routes Mops traffic to stub downstreams that echo which server and path they received. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MopsRoutingTest {

  private static DisposableServer mopsService;
  private static DisposableServer mopsApp;

  @LocalServerPort private int port;

  @BeforeAll
  static void startDownstreams() {
    mopsService = echoServer("service");
    mopsApp = echoServer("app");
  }

  @AfterAll
  static void stopDownstreams() {
    mopsService.disposeNow();
    mopsApp.disposeNow();
  }

  @DynamicPropertySource
  static void downstreamUris(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.mops.service-uri", () -> "http://localhost:" + mopsService.port());
    registry.add("lab.gateway.mops.app-uri", () -> "http://localhost:" + mopsApp.port());
  }

  private static DisposableServer echoServer(String name) {
    return HttpServer.create()
        .port(0)
        .handle((request, response) -> response.sendString(Mono.just(name + " " + request.uri())))
        .bindNow();
  }

  private WebTestClient client() {
    return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void routesApiToServiceWithPrefixStripped() {
    client()
        .post()
        .uri("/mops/api/graphql")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("service /graphql");
  }

  @Test
  void routesEverythingElseUnderMopsToApp() {
    client()
        .get()
        .uri("/mops/spend")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("app /mops/spend");
  }

  @Test
  void keepsAppAssetsUnderThePrefix() {
    client()
        .get()
        .uri("/mops/src/main.js")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("app /mops/src/main.js");
  }

  @Test
  void doesNotRouteOutsideThePrefix() {
    client().get().uri("/spend").exchange().expectStatus().isNotFound();
  }
}
