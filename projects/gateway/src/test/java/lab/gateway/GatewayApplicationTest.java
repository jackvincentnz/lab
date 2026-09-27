package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.test.web.reactive.server.WebTestClient;

class GatewayApplicationTest extends GatewayTestSupport {

  @Autowired private RoutePredicateHandlerMapping routeHandlerMapping;

  @Test
  void context_loadsGatewayRouting() {
    assertThat(routeHandlerMapping).isNotNull();
  }

  @Test
  void health_isPublic() {
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
