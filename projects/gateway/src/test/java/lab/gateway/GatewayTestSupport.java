package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
abstract class GatewayTestSupport {
  static final String PRINCIPAL = "11111111-1111-1111-1111-111111111111";
  static final String TENANT = "22222222-2222-2222-2222-222222222222";
  static final String PASSWORD =
      "{bcrypt}"
          + new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
              .encode("password");
  static final GenericContainer<?> REDIS =
      new GenericContainer<>("redis:7.4-alpine").withExposedPorts(6379);

  static {
    REDIS.start();
  }

  @LocalServerPort int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
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

  static class Browser {
    final WebTestClient client;
    String session;
    String csrf;
    String anonymousSession;
    ResponseCookie cookie;

    Browser(WebTestClient client) {
      this.client = client;
    }

    void form(String path) {
      var request = client.get().uri(path);
      if (session != null) request.cookie("SESSION", session);
      var response =
          request.exchange().expectStatus().isOk().expectBody(String.class).returnResult();
      var nextCookie = response.getResponseCookies().getFirst("SESSION");
      if (nextCookie != null) {
        cookie = nextCookie;
        session = cookie.getValue();
      }
      var matcher =
          Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"")
              .matcher(response.getResponseBody());
      assertThat(matcher.find()).as("CSRF field in the generated form").isTrue();
      csrf = matcher.group(1);
    }

    WebTestClient.ResponseSpec login(String password) {
      form("/login");
      anonymousSession = session;
      return client
          .post()
          .uri("/login")
          .cookie("SESSION", session)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(
              BodyInserters.fromFormData("username", "alice")
                  .with("password", password)
                  .with("_csrf", csrf))
          .exchange();
    }

    Browser login() {
      var response =
          login("password")
              .expectStatus()
              .isFound()
              .expectHeader()
              .valueEquals("Location", "/")
              .expectBody()
              .returnResult();
      cookie = response.getResponseCookies().getFirst("SESSION");
      assertThat(cookie).isNotNull();
      session = cookie.getValue();
      form("/logout");
      return this;
    }

    WebTestClient authenticatedClient() {
      return client
          .mutate()
          .defaultCookie("SESSION", session)
          .defaultHeader("X-CSRF-TOKEN", csrf)
          .build();
    }
  }
}
