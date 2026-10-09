package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
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

/** Proves the record reaches the console as structured JSON and the ID reaches the service. */
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
  void apiRequest_withoutSession_returnsIdWithoutRecord(CapturedOutput output) {
    var requestId =
        client()
            .get()
            .uri("/api/" + randomString())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .returnResult(String.class)
            .getResponseHeaders()
            .getFirst(AccessLogWebFilter.REQUEST_ID);

    assertThat(requestId).isNotBlank();
    assertThat(output.getOut()).doesNotContain(requestId);
  }
}
