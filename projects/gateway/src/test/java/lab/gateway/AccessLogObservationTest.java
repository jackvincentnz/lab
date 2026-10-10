package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.security.web.server.WebFilterExchange;
import reactor.core.publisher.Mono;

class AccessLogObservationTest extends TestBase {

  private final MockServerWebExchange exchange =
      MockServerWebExchange.from(MockServerHttpRequest.get("/" + randomString()));
  private final ServerRequestObservationContext context =
      new ServerRequestObservationContext(
          exchange.getRequest(), exchange.getResponse(), exchange.getAttributes());

  AccessLogObservationTest() {
    exchange
        .getAttributes()
        .put(ServerRequestObservationContext.CURRENT_OBSERVATION_CONTEXT_ATTRIBUTE, context);
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

  private String highCardinality(String key) {
    var value = context.getHighCardinalityKeyValue(key);
    return value == null ? null : value.getValue();
  }

  @Test
  void callerFilter_withGatewayCaller_addsCallerAsHighCardinalityOnly() {
    var principal = principal();

    AccessLogObservation.callerFilter()
        .filter(exchange, next -> Mono.empty())
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(signedIn(principal)))
        .block();

    assertThat(highCardinality(AccessLogObservation.TENANT_ID))
        .isEqualTo(principal.tenant().toString());
    assertThat(highCardinality(AccessLogObservation.PRINCIPAL_ID))
        .isEqualTo(principal.principal().toString());
    assertThat(highCardinality(AccessLogObservation.AUTHENTICATION_METHOD)).isEqualTo(Caller.FORM);
    assertThat(context.getLowCardinalityKeyValues()).isEmpty();
  }

  @Test
  void callerFilter_withoutAuthentication_addsNothingAndContinues() {
    var continued = new AtomicBoolean();

    AccessLogObservation.callerFilter()
        .filter(exchange, next -> Mono.fromRunnable(() -> continued.set(true)))
        .block();

    assertThat(context.getHighCardinalityKeyValues()).isEmpty();
    assertThat(continued).isTrue();
  }

  @Test
  void callerFilter_withOtherAuthentication_addsNothing() {
    AccessLogObservation.callerFilter()
        .filter(exchange, next -> Mono.empty())
        .contextWrite(
            ReactiveSecurityContextHolder.withAuthentication(
                new TestingAuthenticationToken(randomString(), randomString())))
        .block();

    assertThat(context.getHighCardinalityKeyValues()).isEmpty();
  }

  @Test
  void callerOnLogout_withGatewayCaller_addsLoggedOutCaller() {
    var principal = principal();

    AccessLogObservation.callerOnLogout()
        .logout(new WebFilterExchange(exchange, next -> Mono.empty()), signedIn(principal))
        .block();

    assertThat(highCardinality(AccessLogObservation.PRINCIPAL_ID))
        .isEqualTo(principal.principal().toString());
    assertThat(highCardinality(AccessLogObservation.TENANT_ID))
        .isEqualTo(principal.tenant().toString());
  }
}
