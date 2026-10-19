package lab.gateway;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Adds the caller the security chain authenticated to the access record, as {@code tenant_id},
 * {@code principal_id}, and {@code authentication_method}.
 *
 * <p>The filter runs inside the security chain, after authentication, so it is not a component:
 * WebFlux would also run a component filter outside the chain. A request the chain ends before
 * authentication, such as a CSRF rejection, is recorded without a caller.
 */
class CallerWebFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .mapNotNull(SecurityContext::getAuthentication)
        .doOnNext(authentication -> addCaller(exchange, authentication))
        .then(Mono.defer(() -> chain.filter(exchange)));
  }

  /**
   * Adds the caller behind an authentication, or nothing when the gateway did not establish one.
   */
  private static void addCaller(ServerWebExchange exchange, Authentication authentication) {
    Caller.of(authentication)
        .ifPresent(
            caller -> {
              AccessLogObservation.add(
                  exchange, AccessLogObservation.TENANT_ID, caller.tenant().toString());
              AccessLogObservation.add(
                  exchange, AccessLogObservation.PRINCIPAL_ID, caller.principal().toString());
              AccessLogObservation.add(
                  exchange,
                  AccessLogObservation.AUTHENTICATION_METHOD,
                  caller.authenticationMethod());
            });
  }
}
