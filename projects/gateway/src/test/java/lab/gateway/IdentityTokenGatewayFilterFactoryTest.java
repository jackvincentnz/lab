package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.SecurityContextServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class IdentityTokenGatewayFilterFactoryTest extends TestBase {

  private final IdentityTokenMinter minter =
      new IdentityTokenMinter(
          new IdentityTokenProperties(
              randomString(), Duration.ofMinutes(5), Optional.empty(), true),
          SigningKey.generate(),
          fixedClock());
  private final IdentityTokenGatewayFilterFactory factory =
      new IdentityTokenGatewayFilterFactory(minter);
  private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

  private ServerWebExchange request() {
    return MockServerWebExchange.from(
        MockServerHttpRequest.get("/" + randomString())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomString()));
  }

  /** Wraps the exchange the way the security chain does once a session is authenticated. */
  private ServerWebExchange authenticated(ServerWebExchange exchange) {
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
    return new SecurityContextServerWebExchange(
        exchange, Mono.just(new SecurityContextImpl(authentication)));
  }

  private void run(ServerWebExchange exchange) {
    factory
        .apply(config(randomString()))
        .filter(
            exchange,
            next -> {
              forwarded.set(next);
              return Mono.empty();
            })
        .block();
  }

  private static IdentityTokenGatewayFilterFactory.Config config(String audience) {
    var config = new IdentityTokenGatewayFilterFactory.Config();
    config.setAudience(audience);
    return config;
  }

  @Test
  void filter_withoutGatewaySession_refusesTheRequest() {
    assertThatThrownBy(() -> run(request()))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(e -> ((ResponseStatusException) e).getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(forwarded.get()).isNull();
  }

  @Test
  void filter_withGatewaySession_replacesClientAuthorizationWithMintedToken() {
    run(authenticated(request()));

    var authorization = forwarded.get().getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
    assertThat(authorization).hasSize(1);
    assertThat(authorization.get(0)).startsWith("Bearer ey");
  }

  @Test
  void config_withoutAudience_isRejected() {
    assertThatThrownBy(() -> config(" ")).isInstanceOf(IllegalArgumentException.class);
  }
}
