package lab.gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/** Routes Mops traffic to stub downstreams that echo which server and path they received. */
class MopsRoutingTest extends GatewayTestSupport {

  private static DisposableServer mopsService;
  private static DisposableServer mopsApp;

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

  private WebTestClient authenticatedClient() {
    return browser().login().authenticatedClient();
  }

  @Test
  void routesApiToServiceWithPrefixStripped() {
    authenticatedClient()
        .post()
        .uri("/api/graphql")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("service /graphql");
  }

  @Test
  void routesBareApiPathToServiceRoot() {
    authenticatedClient()
        .get()
        .uri("/api")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("service /");
  }

  @Test
  void routesPagesToApp() {
    authenticatedClient()
        .get()
        .uri("/spend")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("app /spend");
  }

  @Test
  void routesAssetsToApp() {
    authenticatedClient()
        .get()
        .uri("/src/main.js")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("app /src/main.js");
  }

  @Test
  void servesHealthFromTheGateway() {
    client()
        .get()
        .uri("/actuator/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("UP");
  }
}
