package lab.gateway;

import io.micrometer.common.KeyValue;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

/**
 * Adds what the gateway learns about a request to the request's observation, which {@link
 * AccessLogHandler} writes as the access record when the observation stops.
 *
 * <p>Fields are high-cardinality key values only, so they never become tags on the {@code
 * http.server.requests} metric.
 */
final class AccessLogObservation {

  static final String REQUEST_ID = "request_id";
  static final String TENANT_ID = "tenant_id";
  static final String PRINCIPAL_ID = "principal_id";
  static final String AUTHENTICATION_METHOD = "authentication_method";

  private AccessLogObservation() {}

  static void add(ServerWebExchange exchange, String key, String value) {
    // HttpWebHandlerAdapter stores the request observation's context in the exchange attributes
    // before any WebFilter runs.
    ServerRequestObservationContext.findCurrent(exchange.getAttributes())
        .ifPresent(context -> context.addHighCardinalityKeyValue(KeyValue.of(key, value)));
  }

  /**
   * Adds the caller behind an authentication, or nothing when the gateway did not establish one.
   */
  private static void addCaller(ServerWebExchange exchange, Authentication authentication) {
    Caller.of(authentication)
        .ifPresent(
            caller -> {
              add(exchange, TENANT_ID, caller.tenant().toString());
              add(exchange, PRINCIPAL_ID, caller.principal().toString());
              add(exchange, AUTHENTICATION_METHOD, caller.authenticationMethod());
            });
  }

  /**
   * Adds the caller the security chain authenticated. A request the chain ends before
   * authentication, such as a CSRF rejection, is recorded without one.
   */
  static WebFilter callerFilter() {
    return (exchange, chain) ->
        ReactiveSecurityContextHolder.getContext()
            .mapNotNull(SecurityContext::getAuthentication)
            .doOnNext(authentication -> addCaller(exchange, authentication))
            .then(Mono.defer(() -> chain.filter(exchange)));
  }
}
