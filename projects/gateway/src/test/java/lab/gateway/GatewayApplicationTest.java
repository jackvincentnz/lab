package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {

  @LocalServerPort private int port;

  @Autowired private RoutePredicateHandlerMapping routeHandlerMapping;

  @Test
  void loadsGatewayRouting() {
    assertThat(routeHandlerMapping).isNotNull();
  }

  @Test
  void answersHealthCheck() {
    WebTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build()
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
