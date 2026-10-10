package lab.gateway;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Gives every request an ID minted here and writes one access record for it. The record names the
 * caller only when the request arrived with a gateway session.
 *
 * <p>The ID replaces any the client sent, so a client cannot choose the ID that downstream services
 * and the access log correlate on. It is forwarded as {@value #REQUEST_ID} and returned on the
 * response under the same name.
 *
 * <p>The filter runs ahead of the security chain so that responses the chain ends early, such as a
 * CSRF rejection or a 401, still carry the ID and are still logged. The caller is read from the
 * session as the request arrived, which is what the security chain authenticates the request with.
 * The record is written as the response commits, once error handlers have set the final status.
 */
@Component
public class AccessLogWebFilter implements WebFilter, Ordered {

  static final String REQUEST_ID = "X-Request-ID";

  private static final Logger log = LoggerFactory.getLogger(AccessLogWebFilter.class);

  private final ServerSecurityContextRepository securityContexts;

  public AccessLogWebFilter(ServerSecurityContextRepository securityContexts) {
    this.securityContexts = securityContexts;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    var requestId = UUID.randomUUID().toString();
    var forwarded =
        exchange
            .mutate()
            .request(request -> request.headers(headers -> headers.set(REQUEST_ID, requestId)))
            .build();
    var response = forwarded.getResponse();
    // Set at commit so a downstream service's own X-Request-ID cannot replace the gateway's.
    response.beforeCommit(
        () -> Mono.fromRunnable(() -> response.getHeaders().set(REQUEST_ID, requestId)));
    return securityContexts
        .load(exchange)
        .mapNotNull(SecurityContext::getAuthentication)
        .map(Caller::of)
        .defaultIfEmpty(Optional.empty())
        .doOnNext(
            caller ->
                response.beforeCommit(
                    () -> Mono.fromRunnable(() -> record(forwarded, caller, requestId))))
        .then(Mono.defer(() -> chain.filter(forwarded)));
  }

  /**
   * Writes named fields only, so no header, cookie, token, or request body value can reach the log.
   * The caller's fields are left out, rather than written empty, when there is no caller.
   */
  private static void record(
      ServerWebExchange exchange, Optional<Caller> caller, String requestId) {
    var request = exchange.getRequest();
    var status = exchange.getResponse().getStatusCode();
    var record = log.atInfo().addKeyValue("source_ip", sourceIp(request));
    if (caller.isPresent()) {
      record =
          record
              .addKeyValue("tenant_id", caller.get().tenant())
              .addKeyValue("principal_id", caller.get().principal())
              .addKeyValue("authentication_method", caller.get().authenticationMethod());
    }
    record
        .addKeyValue("path", request.getPath().value())
        .addKeyValue("status", status == null ? 200 : status.value())
        .addKeyValue("request_id", requestId)
        .log("Access");
  }

  /** The remote address is the socket peer; forwarded headers are not trusted here. */
  private static String sourceIp(ServerHttpRequest request) {
    InetSocketAddress remote = request.getRemoteAddress();
    if (remote == null) {
      return null;
    }
    return remote.getAddress() == null
        ? remote.getHostString()
        : remote.getAddress().getHostAddress();
  }
}
