package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.common.KeyValue;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

class CallerWebFilterTest extends TestBase {

  private final CallerWebFilter callerWebFilter = new CallerWebFilter();
  private final MockServerWebExchange exchange =
      MockServerWebExchange.from(MockServerHttpRequest.get("/" + randomString()));
  private final ServerRequestObservationContext observationContext =
      new ServerRequestObservationContext(
          exchange.getRequest(), exchange.getResponse(), exchange.getAttributes());

  CallerWebFilterTest() {
    exchange
        .getAttributes()
        .put(
            ServerRequestObservationContext.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
            observationContext);
  }

  @Test
  void filter_withGatewayCaller_addsCallerAsHighCardinalityOnly() {
    var gatewayPrincipal = gatewayPrincipal();

    callerWebFilter
        .filter(exchange, next -> Mono.empty())
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(signedIn(gatewayPrincipal)))
        .block();

    assertThat(observationContext.getHighCardinalityKeyValues())
        .containsExactlyInAnyOrder(
            KeyValue.of(AccessLogObservation.TENANT_ID, gatewayPrincipal.tenant().toString()),
            KeyValue.of(AccessLogObservation.PRINCIPAL_ID, gatewayPrincipal.principal().toString()),
            KeyValue.of(AccessLogObservation.AUTHENTICATION_METHOD, Caller.FORM));
    assertThat(observationContext.getLowCardinalityKeyValues()).isEmpty();
  }

  @Test
  void filter_withoutAuthentication_addsNothingAndContinues() {
    var continued = new AtomicBoolean();

    callerWebFilter.filter(exchange, next -> Mono.fromRunnable(() -> continued.set(true))).block();

    assertThat(observationContext.getHighCardinalityKeyValues()).isEmpty();
    assertThat(continued).isTrue();
  }

  @Test
  void filter_withOtherAuthentication_addsNothing() {
    var testingAuthenticationToken = new TestingAuthenticationToken(randomString(), randomString());

    callerWebFilter
        .filter(exchange, next -> Mono.empty())
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(testingAuthenticationToken))
        .block();

    assertThat(observationContext.getHighCardinalityKeyValues()).isEmpty();
  }

  private GatewayPrincipal gatewayPrincipal() {
    return new GatewayPrincipal(
        new GatewayUsers.ConfiguredUser(
            randomString(),
            "{noop}" + randomString(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(randomString())));
  }

  private Authentication signedIn(GatewayPrincipal gatewayPrincipal) {
    return UsernamePasswordAuthenticationToken.authenticated(
        gatewayPrincipal, null, gatewayPrincipal.getAuthorities());
  }
}
