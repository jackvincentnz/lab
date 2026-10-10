package lab.gateway;

import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Gives every request an ID minted here, forwards it downstream and returns it to the client as
 * {@value #HEADER}, and adds it to the access record.
 *
 * <p>The ID replaces any the client sent, so a client cannot choose the ID that downstream services
 * and the access log correlate on. The filter runs ahead of the security chain so that responses
 * the chain ends early, such as a CSRF rejection or a 401, still carry the ID.
 */
@Component
public class RequestIdWebFilter implements WebFilter, Ordered {

  static final String HEADER = "X-Request-ID";

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var requestId = UUID.randomUUID().toString();
    AccessLogObservation.add(exchange, AccessLogObservation.REQUEST_ID, requestId);
    var forwarded =
        exchange
            .mutate()
            .request(request -> request.headers(headers -> headers.set(HEADER, requestId)))
            .build();
    var response = forwarded.getResponse();
    // Set at commit so a downstream service's own X-Request-ID cannot replace the gateway's.
    response.beforeCommit(
        () -> Mono.fromRunnable(() -> response.getHeaders().set(HEADER, requestId)));
    return chain.filter(forwarded);
  }
}
