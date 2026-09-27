package lab.gateway;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Exposes Spring's session-bound token for browser API requests. */
@RestController
public class CsrfController {
  @GetMapping("/api/csrf")
  public Mono<CsrfToken> csrf(ServerWebExchange exchange) {
    return exchange.getRequiredAttribute(CsrfToken.class.getName());
  }
}
