package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

class RequestIdWebFilterTest extends TestBase {

  private final RequestIdWebFilter filter = new RequestIdWebFilter();
  private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

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
    filter
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

  @Test
  void filter_anyRequest_addsReturnedIdToObservation() {
    var exchange = exchange(MockServerHttpRequest.get("/" + randomString()));

    run(exchange);

    var context = ServerRequestObservationContext.findCurrent(exchange.getAttributes()).get();
    assertThat(context.getHighCardinalityKeyValue(AccessLogObservation.REQUEST_ID).getValue())
        .isEqualTo(returned(exchange));
    assertThat(context.getLowCardinalityKeyValue(AccessLogObservation.REQUEST_ID)).isNull();
  }

  @Test
  void filter_withClientRequestId_forwardsAndReturnsMintedId() {
    var clientId = randomString();
    var exchange =
        exchange(
            MockServerHttpRequest.get("/" + randomString())
                .header(RequestIdWebFilter.HEADER, clientId));

    run(exchange);

    var sent = forwarded.get().getRequest().getHeaders().get(RequestIdWebFilter.HEADER);
    assertThat(sent).hasSize(1);
    assertThat(UUID.fromString(sent.get(0))).isNotNull();
    assertThat(sent.get(0)).isNotEqualTo(clientId);
    assertThat(exchange.getResponse().getHeaders().get(RequestIdWebFilter.HEADER))
        .containsExactly(sent.get(0));
  }

  @Test
  void filter_withDownstreamRequestId_returnsGatewayId() {
    var exchange = exchange(MockServerHttpRequest.get("/" + randomString()));

    filter
        .filter(
            exchange,
            next -> {
              forwarded.set(next);
              next.getResponse().getHeaders().set(RequestIdWebFilter.HEADER, randomString());
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
}
