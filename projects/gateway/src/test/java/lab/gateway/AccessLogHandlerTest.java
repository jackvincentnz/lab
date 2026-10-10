package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lab.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

class AccessLogHandlerTest extends TestBase {

  private final AccessLogHandler handler = new AccessLogHandler();
  private final ListAppender<ILoggingEvent> records = new ListAppender<>();
  private final Logger logger = (Logger) LoggerFactory.getLogger(AccessLogHandler.class);

  @BeforeEach
  void captureRecords() {
    records.start();
    logger.addAppender(records);
  }

  @AfterEach
  void releaseRecords() {
    logger.detachAppender(records);
  }

  private String sourceIp() {
    return "192.0.2." + (1 + randomInt() % 254);
  }

  private ServerRequestObservationContext context(MockServerHttpRequest request) {
    return new ServerRequestObservationContext(
        request, new MockServerHttpResponse(), new HashMap<>());
  }

  private void observe(ServerRequestObservationContext context, HttpStatus status) {
    handler.onStart(context);
    context.getResponse().setStatusCode(status);
    handler.onStop(context);
  }

  private Map<String, Object> onlyRecord() {
    assertThat(records.list).hasSize(1);
    assertThat(records.list.get(0).getFormattedMessage()).isEqualTo("Access");
    return records.list.get(0).getKeyValuePairs().stream()
        .collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
  }

  @Test
  void onStop_withCaller_logsEveryField() {
    var path = "/api/" + randomString();
    var sourceIp = sourceIp();
    var context =
        context(
            MockServerHttpRequest.post(path + "?q=" + randomString())
                .remoteAddress(new InetSocketAddress(sourceIp, 40000))
                .build());
    var requestId = UUID.randomUUID().toString();
    var tenant = UUID.randomUUID().toString();
    var principal = UUID.randomUUID().toString();
    var method = randomString();
    context.addHighCardinalityKeyValue(KeyValue.of(AccessLogObservation.REQUEST_ID, requestId));
    context.addHighCardinalityKeyValue(KeyValue.of(AccessLogObservation.TENANT_ID, tenant));
    context.addHighCardinalityKeyValue(KeyValue.of(AccessLogObservation.PRINCIPAL_ID, principal));
    context.addHighCardinalityKeyValue(
        KeyValue.of(AccessLogObservation.AUTHENTICATION_METHOD, method));

    observe(context, HttpStatus.ACCEPTED);

    var record = onlyRecord();
    assertThat(record)
        .containsOnlyKeys(
            "method",
            "path",
            "status",
            "duration_ms",
            "source_ip",
            "request_id",
            "tenant_id",
            "principal_id",
            "authentication_method")
        .containsEntry("method", "POST")
        .containsEntry("path", path)
        .containsEntry("status", HttpStatus.ACCEPTED.value())
        .containsEntry("source_ip", sourceIp)
        .containsEntry("request_id", requestId)
        .containsEntry("tenant_id", tenant)
        .containsEntry("principal_id", principal)
        .containsEntry("authentication_method", method);
    assertThat((Long) record.get("duration_ms")).isNotNegative();
  }

  @Test
  void onStop_withoutCaller_omitsCallerFields() {
    var path = "/" + randomString();
    var context =
        context(
            MockServerHttpRequest.get(path)
                .remoteAddress(new InetSocketAddress(sourceIp(), 40000))
                .build());
    context.addHighCardinalityKeyValue(
        KeyValue.of(AccessLogObservation.REQUEST_ID, UUID.randomUUID().toString()));

    observe(context, HttpStatus.UNAUTHORIZED);

    assertThat(onlyRecord())
        .containsOnlyKeys("method", "path", "status", "duration_ms", "source_ip", "request_id")
        .containsEntry("path", path)
        .containsEntry("status", HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  void onStop_withNothingAdded_omitsUnknownFields() {
    var context = context(MockServerHttpRequest.get("/" + randomString()).build());

    handler.onStop(context);

    assertThat(onlyRecord()).doesNotContainKeys("duration_ms", "source_ip", "request_id");
  }

  @Test
  void onStop_withAbortedConnection_omitsStatus() {
    var context = context(MockServerHttpRequest.get("/" + randomString()).build());
    context.setConnectionAborted(true);

    observe(context, HttpStatus.OK);

    assertThat(onlyRecord()).doesNotContainKey("status");
  }

  @Test
  void onStop_withCredentials_neverLogsThem() {
    var bearer = randomString();
    var cookie = randomString();
    var password = randomString();
    var context =
        context(
            MockServerHttpRequest.post("/login?password=" + password)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                .header(HttpHeaders.COOKIE, "SESSION=" + cookie)
                .remoteAddress(new InetSocketAddress(sourceIp(), 40000))
                .build());

    observe(context, HttpStatus.FOUND);

    var event = records.list.get(0);
    var logged = event.getFormattedMessage() + " " + event.getKeyValuePairs();
    assertThat(logged).doesNotContain(bearer).doesNotContain(cookie).doesNotContain(password);
  }

  @Test
  void supportsContext_otherContext_isFalse() {
    assertThat(handler.supportsContext(new Observation.Context())).isFalse();
  }
}
