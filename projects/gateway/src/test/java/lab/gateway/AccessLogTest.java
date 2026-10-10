package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
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

  /** The console line carrying {@code requestId}, parsed. */
  private static Map<String, Object> record(CapturedOutput output, String requestId) {
    var lines =
        output
            .getOut()
            .lines()
            .filter(line -> line.startsWith("{") && line.contains(requestId))
            .toList();
    assertThat(lines).hasSize(1);
    return JsonPath.parse(lines.get(0)).read("$");
  }

  @Test
  void apiRequest_withSession_logsRecordWithTheIdTheServiceReceived(CapturedOutput output) {
    var path = "/api/" + randomString();
    var result =
        browser()
            .login()
            .authenticatedClient()
            .get()
            .uri(path)
            .header(AccessLogWebFilter.REQUEST_ID, randomString())
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class);
    var requestId = result.getResponseHeaders().getFirst(AccessLogWebFilter.REQUEST_ID);

    assertThat(result.getResponseHeaders().get(Downstream.REQUEST_ID)).containsExactly(requestId);
    assertThat(record(output, requestId))
        .containsEntry("message", "Access")
        .containsEntry("tenant_id", USER.tenant().toString())
        .containsEntry("principal_id", USER.principal().toString())
        .containsEntry("authentication_method", Caller.FORM)
        .containsEntry("path", path)
        .containsEntry("status", 200)
        .containsKey("source_ip");
  }

  @Test
  void apiRequest_withBearerToken_logsRecordWithTheTokenCaller(CapturedOutput output) {
    var caller =
        new GatewayUsers.ConfiguredUser(
            randomString(),
            "{noop}" + randomString(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(randomString()));
    var path = "/api/" + randomString();

    var requestId =
        client()
            .get()
            .uri(path)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer(caller).mint())
            .exchange()
            .expectStatus()
            .isOk()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(AccessLogWebFilter.REQUEST_ID);

    assertThat(record(output, requestId))
        .containsEntry("tenant_id", caller.tenant().toString())
        .containsEntry("principal_id", caller.principal().toString())
        .containsEntry("authentication_method", Caller.BEARER)
        .containsEntry("path", path)
        .containsEntry("status", 200);
  }

  @Test
  void apiRequest_withoutSession_logsRecordWithoutCallerFields(CapturedOutput output) {
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
            .getFirst(AccessLogWebFilter.REQUEST_ID);

    assertThat(record(output, requestId))
        .containsEntry("path", path)
        .containsEntry("status", 401)
        .containsEntry("request_id", requestId)
        .containsKey("source_ip")
        .doesNotContainKeys("tenant_id", "principal_id", "authentication_method");
  }

  @Test
  void loginSubmission_withWrongPassword_logsRecordWithoutThePassword(CapturedOutput output) {
    var password = randomString();

    var requestId =
        browser()
            .login(password)
            .expectStatus()
            .isFound()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(AccessLogWebFilter.REQUEST_ID);

    assertThat(record(output, requestId))
        .containsEntry("path", "/login")
        .containsEntry("status", 302)
        .doesNotContainKeys("tenant_id", "principal_id", "authentication_method");
    assertThat(output.getOut()).doesNotContain(password);
  }
}
