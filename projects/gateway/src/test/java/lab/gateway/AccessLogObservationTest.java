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

class AccessLogObservationTest extends TestBase {

  private final MockServerWebExchange exchange =
      MockServerWebExchange.from(MockServerHttpRequest.get("/" + randomString()));
  private final ServerRequestObservationContext observationContext =
      new ServerRequestObservationContext(
          exchange.getRequest(), exchange.getResponse(), exchange.getAttributes());

  AccessLogObservationTest() {
    exchange
        .getAttributes()
        .put(
            ServerRequestObservationContext.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE,
            observationContext);
  }

  private Authentication signedIn(GatewayPrincipal principal) {
    return UsernamePasswordAuthenticationToken.authenticated(
        principal, null, principal.getAuthorities());
  }

  private GatewayPrincipal principal() {
    return new GatewayPrincipal(
        new GatewayUsers.ConfiguredUser(
            randomString(),
            "{noop}" + randomString(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(randomString())));
  }

  @Test
  void callerFilter_withGatewayCaller_addsCallerAsHighCardinalityOnly() {
    var principal = principal();

    AccessLogObservation.callerFilter()
        .filter(exchange, next -> Mono.empty())
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(signedIn(principal)))
        .block();

    assertThat(observationContext.getHighCardinalityKeyValues())
        .containsExactlyInAnyOrder(
            KeyValue.of(AccessLogObservation.TENANT_ID, principal.tenant().toString()),
            KeyValue.of(AccessLogObservation.PRINCIPAL_ID, principal.principal().toString()),
            KeyValue.of(AccessLogObservation.AUTHENTICATION_METHOD, Caller.FORM));
    assertThat(observationContext.getLowCardinalityKeyValues()).isEmpty();
  }

  @Test
  void callerFilter_withoutAuthentication_addsNothingAndContinues() {
    var continued = new AtomicBoolean();

    AccessLogObservation.callerFilter()
        .filter(exchange, next -> Mono.fromRunnable(() -> continued.set(true)))
        .block();

    assertThat(observationContext.getHighCardinalityKeyValues()).isEmpty();
    assertThat(continued).isTrue();
  }

  @Test
  void callerFilter_withOtherAuthentication_addsNothing() {
    var authentication = new TestingAuthenticationToken(randomString(), randomString());

    AccessLogObservation.callerFilter()
        .filter(exchange, next -> Mono.empty())
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        .block();

    assertThat(observationContext.getHighCardinalityKeyValues()).isEmpty();
  }
}
