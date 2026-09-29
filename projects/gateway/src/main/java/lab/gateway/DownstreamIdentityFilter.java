package lab.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Replaces every client Authorization value after session authentication and before routing. */
@Component
public final class DownstreamIdentityFilter implements GlobalFilter, Ordered {
  private final DownstreamIdentity identity;

  public DownstreamIdentityFilter(DownstreamIdentity identity) {
    this.identity = identity;
  }

  @Override
  public int getOrder() {
    return -100;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return exchange
        .getPrincipal()
        .ofType(Authentication.class)
        .filter(Authentication::isAuthenticated)
        .map(Authentication::getPrincipal)
        .ofType(GatewayPrincipal.class)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
        .zipWith(exchange.getSession())
        .flatMap(
            caller -> {
              var token = identity.mint(caller.getT1(), caller.getT2().getId());
              var request =
                  exchange
                      .getRequest()
                      .mutate()
                      .headers(headers -> headers.setBearerAuth(token))
                      .build();
              return chain.filter(exchange.mutate().request(request).build());
            });
  }
}
