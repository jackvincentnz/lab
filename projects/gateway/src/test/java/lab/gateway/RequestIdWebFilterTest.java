package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.common.KeyValue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

class RequestIdWebFilterTest extends TestBase {

  private final RequestIdWebFilter requestIdWebFilter = new RequestIdWebFilter();
  private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

  @Test
  void filter_anyRequest_addsReturnedIdToObservation() {
    var exchange = exchange(MockServerHttpRequest.get("/" + randomString()));

    run(exchange);

    var observationContext =
        ServerRequestObservationContext.findCurrent(exchange.getAttributes()).orElseThrow();
    assertThat(observationContext.getHighCardinalityKeyValues())
        .containsExactly(KeyValue.of(AccessLogObservation.REQUEST_ID, returned(exchange)));
    assertThat(observationContext.getLowCardinalityKeyValues()).isEmpty();
  }

  @Test
  void filter_withClientRequestId_forwardsAndReturnsMintedId() {
    var clientId = randomString();
    var exchange =
        exchange(
            MockServerHttpRequest.get("/" + randomString())
                .header(RequestIdWebFilter.HEADER, clientId));

    run(exchange);

    var forwardedIds = forwarded.get().getRequest().getHeaders().get(RequestIdWebFilter.HEADER);
    assertThat(forwardedIds)
        .singleElement()
        .isNotEqualTo(clientId)
        .isEqualTo(UUID.fromString(forwardedIds.get(0)).toString());
    assertThat(exchange.getResponse().getHeaders().get(RequestIdWebFilter.HEADER))
        .isEqualTo(forwardedIds);
  }

  @Test
  void filter_withDownstreamRequestId_returnsGatewayId() {
    var exchange = exchange(MockServerHttpRequest.get("/" + randomString()));
    var downstreamId = randomString();

    requestIdWebFilter
        .filter(
            exchange,
            next -> {
              forwarded.set(next);
              next.getResponse().getHeaders().set(RequestIdWebFilter.HEADER, downstreamId);
              return next.getResponse().setComplete();
            })
        .block();

    assertThat(exchange.getResponse().getHeaders().get(RequestIdWebFilter.HEADER))
        .containsExactly(
            forwarded.get().getRequest().getHeaders().getFirst(RequestIdWebFilter.HEADER));
  }

  @Test
  void filter_eachRequest_mintsADifferentId() {
    var first = exchange(MockServerHttpRequest.get("/" + randomString()));
    var second = exchange(MockServerHttpRequest.get("/" + randomString()));

    run(first);
    run(second);

    assertThat(returned(first)).isNotEqualTo(returned(second));
  }

  /** An exchange carrying a request observation, the way HttpWebHandlerAdapter hands it over. */
  private MockServerWebExchange exchange(MockServerHttpRequest.BaseBuilder<?> request) {
    var exchange = MockServerWebExchange.from(request);
    exchange
        .getAttributes()
        .put(
            ServerRequestObservationContext.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
            new ServerRequestObservationContext(
                exchange.getRequest(), exchange.getResponse(), exchange.getAttributes()));
    return exchange;
  }

  private void run(ServerWebExchange exchange) {
    requestIdWebFilter
        .filter(
            exchange,
            next -> {
              forwarded.set(next);
              return next.getResponse().setComplete();
            })
        .block();
  }

  private static String returned(ServerWebExchange exchange) {
    return exchange.getResponse().getHeaders().getFirst(RequestIdWebFilter.HEADER);
  }
}
