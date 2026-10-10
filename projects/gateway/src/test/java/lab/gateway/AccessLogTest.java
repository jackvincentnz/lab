package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Proves records reach the console as structured JSON and the ID reaches the service. */
@ExtendWith(OutputCaptureExtension.class)
class AccessLogTest extends GatewayTestSupport {

  private static Downstream mopsService;

  @BeforeAll
  static void startDownstream() {
    mopsService = Downstream.start("service");
  }

  @AfterAll
  static void stopDownstream() {
    mopsService.close();
  }

  @DynamicPropertySource
  static void downstreamUris(DynamicPropertyRegistry registry) {
    registry.add("lab.gateway.mops.service-uri", mopsService::uri);
  }

  /**
   * The access record carrying {@code requestId}, parsed. The request's observation stops once the
   * response is complete, which can be just after the client has read it, so this waits briefly.
   */
  private static Map<String, Object> record(CapturedOutput output, String requestId)
      throws InterruptedException {
    var deadline = Instant.now().plus(Duration.ofSeconds(5));
    var lines = output.getOut().lines().filter(line -> isRecord(line, requestId)).toList();
    while (lines.isEmpty() && Instant.now().isBefore(deadline)) {
      Thread.sleep(10);
      lines = output.getOut().lines().filter(line -> isRecord(line, requestId)).toList();
    }
    assertThat(lines).hasSize(1);
    return JsonPath.parse(lines.get(0)).read("$");
  }

  private static boolean isRecord(String line, String requestId) {
    return line.startsWith("{") && line.contains("\"Access\"") && line.contains(requestId);
  }

  @Test
  void anyRequest_logsRecord(CapturedOutput output) throws InterruptedException {
    var requestId =
        client()
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(RequestIdWebFilter.HEADER);

    assertThat(record(output, requestId))
        .containsEntry("message", "Access")
        .containsEntry("method", "GET")
        .containsEntry("path", "/actuator/health")
        .containsEntry("status", 200)
        .containsEntry("request_id", requestId)
        .containsKeys("source_ip", "duration_ms");
  }

  @Test
  void apiRequest_withSession_logsCallerAndTheIdTheServiceReceived(CapturedOutput output)
      throws InterruptedException {
    var path = "/api/" + randomString();
    var clientId = randomString();
    var result =
        browser()
            .login()
            .authenticatedClient()
            .get()
            .uri(path + "?q=" + randomString())
            .header(RequestIdWebFilter.HEADER, clientId)
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class);
    var requestId = result.getResponseHeaders().getFirst(RequestIdWebFilter.HEADER);

    assertThat(requestId).isNotEqualTo(clientId);
    assertThat(result.getResponseHeaders().get(Downstream.REQUEST_ID)).containsExactly(requestId);
    assertThat(record(output, requestId))
        .containsEntry("tenant_id", USER.tenant().toString())
        .containsEntry("principal_id", USER.principal().toString())
        .containsEntry("authentication_method", Caller.FORM)
        .containsEntry("method", "GET")
        .containsEntry("path", path)
        .containsEntry("status", 200)
        .containsEntry("request_id", requestId)
        .containsKeys("source_ip", "duration_ms");
    assertThat(output.getOut()).doesNotContain(clientId);
  }

  @Test
  void apiRequest_withoutSession_logsRecordWithoutCallerFields(CapturedOutput output)
      throws InterruptedException {
    var path = "/api/" + randomString();
    var requestId =
        client()
            .get()
            .uri(path)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(RequestIdWebFilter.HEADER);

    assertThat(record(output, requestId))
        .containsEntry("path", path)
        .containsEntry("status", 401)
        .containsEntry("request_id", requestId)
        .containsKey("source_ip")
        .doesNotContainKeys("tenant_id", "principal_id", "authentication_method");
  }

  @Test
  void loginSubmission_withWrongPassword_logsRecordWithoutThePassword(CapturedOutput output)
      throws InterruptedException {
    var password = randomString();

    var requestId =
        browser()
            .login(password)
            .expectStatus()
            .isFound()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(RequestIdWebFilter.HEADER);

    assertThat(record(output, requestId))
        .containsEntry("method", "POST")
        .containsEntry("path", "/login")
        .containsEntry("status", 302)
        .doesNotContainKeys("tenant_id", "principal_id", "authentication_method");
    assertThat(output.getOut()).doesNotContain(password);
  }

  @Test
  void logout_withSession_logsTheCallerWhoLoggedOut(CapturedOutput output)
      throws InterruptedException {
    var requestId =
        browser()
            .login()
            .authenticatedClient()
            .post()
            .uri("/logout")
            .exchange()
            .expectStatus()
            .isFound()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(RequestIdWebFilter.HEADER);

    assertThat(record(output, requestId))
        .containsEntry("path", "/logout")
        .containsEntry("status", 302)
        .containsEntry("tenant_id", USER.tenant().toString())
        .containsEntry("principal_id", USER.principal().toString())
        .containsEntry("authentication_method", Caller.FORM);
  }

  @Test
  void unsafeRequest_withoutCsrfToken_logsRejection(CapturedOutput output)
      throws InterruptedException {
    var path = "/api/" + randomString();
    var browser = browser().login();

    var requestId =
        client()
            .post()
            .uri(path)
            .cookie("SESSION", browser.session)
            .exchange()
            .expectStatus()
            .isForbidden()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(RequestIdWebFilter.HEADER);

    assertThat(record(output, requestId))
        .containsEntry("method", "POST")
        .containsEntry("path", path)
        .containsEntry("status", 403)
        .containsEntry("request_id", requestId);
  }
}
