package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {

  @Value("${local.server.port}")
  private int port;

  @Autowired private RoutePredicateHandlerMapping gatewayHandlerMapping;

  @Test
  void gatewayStartsAndServesPublicHealth() throws Exception {
    assertThat(gatewayHandlerMapping).isNotNull();

    HttpResponse<String> response =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
            .send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/health"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(JsonPath.<String>read(response.body(), "$.status")).isEqualTo("UP");
  }
}
