package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lab.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

class AccessLogWebFilterTest extends TestBase {

  private final WebSessionServerSecurityContextRepository securityContexts =
      new WebSessionServerSecurityContextRepository();
  private final AccessLogWebFilter filter = new AccessLogWebFilter(securityContexts);
  private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
  private final ListAppender<ILoggingEvent> records = new ListAppender<>();
  private final Logger logger = (Logger) LoggerFactory.getLogger(AccessLogWebFilter.class);

  @BeforeEach
  void captureRecords() {
    records.start();
    logger.addAppender(records);
  }

  @AfterEach
  void releaseRecords() {
    logger.detachAppender(records);
  }

  private MockServerHttpRequest.BaseBuilder<?> request(String path) {
    return MockServerHttpRequest.get(path)
        .remoteAddress(new InetSocketAddress("192.0.2." + (1 + randomInt() % 254), 40000));
  }

  /** Stores an authenticated context in the session, the way form login leaves it. */
  private GatewayPrincipal signIn(ServerWebExchange exchange) {
    var principal =
        new GatewayPrincipal(
            new GatewayUsers.ConfiguredUser(
                randomString(),
                "{noop}" + randomString(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(randomString())));
    var authentication =
        UsernamePasswordAuthenticationToken.authenticated(
            principal, null, principal.getAuthorities());
    securityContexts.save(exchange, new SecurityContextImpl(authentication)).block();
    return principal;
  }

  private void run(ServerWebExchange exchange, HttpStatus status) {
    filter
        .filter(
            exchange,
            next -> {
              forwarded.set(next);
              next.getResponse().setStatusCode(status);
              return next.getResponse().setComplete();
            })
        .block();
  }

  private Map<String, Object> onlyRecord() {
    assertThat(records.list).hasSize(1);
    return records.list.get(0).getKeyValuePairs().stream()
        .collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
  }

  @Test
  void filter_withSession_logsCallerPathStatusAndSource() {
    var path = "/api/" + randomString();
    var exchange = MockServerWebExchange.from(request(path + "?q=" + randomString()));
    var principal = signIn(exchange);

    run(exchange, HttpStatus.ACCEPTED);

    var record = onlyRecord();
    assertThat(record)
        .containsEntry(
            "source_ip", exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())
        .containsEntry("tenant_id", principal.tenant())
        .containsEntry("principal_id", principal.principal())
        .containsEntry("authentication_method", Caller.FORM)
        .containsEntry("path", path)
        .containsEntry("status", HttpStatus.ACCEPTED.value())
        .containsEntry(
            "request_id",
            exchange.getResponse().getHeaders().getFirst(AccessLogWebFilter.REQUEST_ID));
  }

  @Test
  void filter_withSession_neverLogsCredentials() {
    var bearer = randomString();
    var cookie = randomString();
    var exchange =
        MockServerWebExchange.from(
            request("/" + randomString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                .header(HttpHeaders.COOKIE, "SESSION=" + cookie));
    signIn(exchange);

    run(exchange, HttpStatus.OK);

    var event = records.list.get(0);
    var logged = event.getFormattedMessage() + " " + event.getKeyValuePairs();
    assertThat(logged).doesNotContain(bearer).doesNotContain(cookie);
  }

  @Test
  void filter_withoutSession_logsNothing() {
    var exchange = MockServerWebExchange.from(request("/" + randomString()));

    run(exchange, HttpStatus.UNAUTHORIZED);

    assertThat(records.list).isEmpty();
  }

  @Test
  void filter_withClientRequestId_forwardsAndReturnsMintedId() {
    var clientId = randomString();
    var exchange =
        MockServerWebExchange.from(
            request("/" + randomString()).header(AccessLogWebFilter.REQUEST_ID, clientId));

    run(exchange, HttpStatus.OK);

    var sent = forwarded.get().getRequest().getHeaders().get(AccessLogWebFilter.REQUEST_ID);
    assertThat(sent).hasSize(1);
    assertThat(UUID.fromString(sent.get(0))).isNotNull();
    assertThat(sent.get(0)).isNotEqualTo(clientId);
    assertThat(exchange.getResponse().getHeaders().get(AccessLogWebFilter.REQUEST_ID))
        .containsExactly(sent.get(0));
  }

  @Test
  void filter_withDownstreamRequestId_returnsGatewayId() {
    var exchange = MockServerWebExchange.from(request("/" + randomString()));

    filter
        .filter(
            exchange,
            next -> {
              forwarded.set(next);
              next.getResponse().getHeaders().set(AccessLogWebFilter.REQUEST_ID, randomString());
              return next.getResponse().setComplete();
            })
        .block();

    assertThat(exchange.getResponse().getHeaders().get(AccessLogWebFilter.REQUEST_ID))
        .containsExactly(
            forwarded.get().getRequest().getHeaders().getFirst(AccessLogWebFilter.REQUEST_ID));
  }

  @Test
  void filter_eachRequest_mintsADifferentId() {
    var first = MockServerWebExchange.from(request("/" + randomString()));
    var second = MockServerWebExchange.from(request("/" + randomString()));

    run(first, HttpStatus.OK);
    run(second, HttpStatus.OK);

    assertThat(first.getResponse().getHeaders().getFirst(AccessLogWebFilter.REQUEST_ID))
        .isNotEqualTo(second.getResponse().getHeaders().getFirst(AccessLogWebFilter.REQUEST_ID));
  }
}
