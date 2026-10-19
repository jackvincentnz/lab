package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
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

  private final AccessLogHandler accessLogHandler = new AccessLogHandler();
  private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
  private final Logger logger = (Logger) LoggerFactory.getLogger(AccessLogHandler.class);

  @BeforeEach
  void captureRecords() {
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @AfterEach
  void releaseRecords() {
    logger.detachAppender(listAppender);
  }

  private String sourceIp() {
    return "192.0.2." + (1 + randomInt() % 254);
  }

  private int port() {
    return 1024 + randomInt() % 64512;
  }

  private ServerRequestObservationContext observationContext(MockServerHttpRequest request) {
    return new ServerRequestObservationContext(
        request, new MockServerHttpResponse(), new HashMap<>());
  }

  private void observe(ServerRequestObservationContext observationContext, HttpStatus status) {
    accessLogHandler.onStart(observationContext);
    observationContext.getResponse().setStatusCode(status);
    accessLogHandler.onStop(observationContext);
  }

  private Map<String, Object> onlyRecord() {
    assertThat(listAppender.list).hasSize(1);
    assertThat(listAppender.list.get(0).getFormattedMessage()).isEqualTo("Access");
    return listAppender.list.get(0).getKeyValuePairs().stream()
        .collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
  }

  @Test
  void onStop_withCaller_logsEveryField() {
    var path = "/api/" + randomString();
    var sourceIp = sourceIp();
    var observationContext =
        observationContext(
            MockServerHttpRequest.post(path + "?q=" + randomString())
                .remoteAddress(new InetSocketAddress(sourceIp, port()))
                .build());

    var requestId = randomId();
    var tenant = randomId();
    var principal = randomId();
    var method = randomString();
    observationContext.addHighCardinalityKeyValues(
        KeyValues.of(
            KeyValue.of(AccessLogObservation.REQUEST_ID, requestId),
            KeyValue.of(AccessLogObservation.TENANT_ID, tenant),
            KeyValue.of(AccessLogObservation.PRINCIPAL_ID, principal),
            KeyValue.of(AccessLogObservation.AUTHENTICATION_METHOD, method)));

    observe(observationContext, HttpStatus.ACCEPTED);

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
    var observationContext =
        observationContext(
            MockServerHttpRequest.get(path)
                .remoteAddress(new InetSocketAddress(sourceIp(), port()))
                .build());
    observationContext.addHighCardinalityKeyValue(
        KeyValue.of(AccessLogObservation.REQUEST_ID, randomId()));

    observe(observationContext, HttpStatus.UNAUTHORIZED);

    assertThat(onlyRecord())
        .containsOnlyKeys("method", "path", "status", "duration_ms", "source_ip", "request_id")
        .containsEntry("path", path)
        .containsEntry("status", HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  void onStop_withNothingAdded_omitsUnknownFields() {
    var observationContext =
        observationContext(MockServerHttpRequest.get("/" + randomString()).build());

    accessLogHandler.onStart(observationContext);
    accessLogHandler.onStop(observationContext);

    assertThat(onlyRecord()).doesNotContainKeys("status", "source_ip", "request_id");
  }

  @Test
  void onStop_abortedBeforeCommit_omitsStatus() {
    var observationContext =
        observationContext(MockServerHttpRequest.get("/" + randomString()).build());
    observationContext.setConnectionAborted(true);

    observe(observationContext, HttpStatus.OK);

    assertThat(onlyRecord()).doesNotContainKey("status");
  }

  @Test
  void onStop_abortedAfterCommit_logsTheStatus() {
    var observationContext =
        observationContext(MockServerHttpRequest.get("/" + randomString()).build());
    accessLogHandler.onStart(observationContext);

    observationContext.getResponse().setStatusCode(HttpStatus.OK);
    observationContext.getResponse().setComplete().block();
    observationContext.setConnectionAborted(true);

    accessLogHandler.onStop(observationContext);

    assertThat(onlyRecord()).containsEntry("status", HttpStatus.OK.value());
  }

  @Test
  void onStop_withUnresolvedRemoteAddress_logsItsHostAsGiven() {
    var host = randomString();
    var observationContext =
        observationContext(
            MockServerHttpRequest.get("/" + randomString())
                .remoteAddress(InetSocketAddress.createUnresolved(host, port()))
                .build());

    observe(observationContext, HttpStatus.OK);

    assertThat(onlyRecord()).containsEntry("source_ip", host);
  }

  @Test
  void onStop_withCredentials_neverLogsThem() {
    var bearer = randomString();
    var cookie = randomString();
    var password = randomString();
    var observationContext =
        observationContext(
            MockServerHttpRequest.post("/login?password=" + password)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                .header(HttpHeaders.COOKIE, "SESSION=" + cookie)
                .remoteAddress(new InetSocketAddress(sourceIp(), port()))
                .build());

    observe(observationContext, HttpStatus.FOUND);

    var loggingEvent = listAppender.list.get(0);
    var logged = loggingEvent.getFormattedMessage() + " " + loggingEvent.getKeyValuePairs();
    assertThat(logged).doesNotContain(bearer).doesNotContain(cookie).doesNotContain(password);
  }

  @Test
  void supportsContext_withServerRequestContext_isTrue() {
    var observationContext =
        observationContext(MockServerHttpRequest.get("/" + randomString()).build());

    var supported = accessLogHandler.supportsContext(observationContext);

    assertThat(supported).isTrue();
  }

  @Test
  void supportsContext_withOtherContext_isFalse() {
    var observationContext = new Observation.Context();

    var supported = accessLogHandler.supportsContext(observationContext);

    assertThat(supported).isFalse();
  }
}
