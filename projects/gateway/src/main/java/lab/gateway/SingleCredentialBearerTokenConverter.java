package lab.gateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Refuses any {@code Authorization} header beside a live session, so no credential silently wins.
 */
class SingleCredentialBearerTokenConverter implements ServerAuthenticationConverter {

  private static final BearerTokenError SESSION_AND_AUTHORIZATION =
      new BearerTokenError(
          BearerTokenErrorCodes.INVALID_REQUEST,
          HttpStatus.BAD_REQUEST,
          "Send a session or an Authorization header, not both",
          "https://tools.ietf.org/html/rfc6750#section-3.1");

  private final ServerAuthenticationConverter bearerTokenConverter =
      new ServerBearerTokenAuthenticationConverter();

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    if (!exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
      return bearerTokenConverter.convert(exchange);
    }
    return hasLoggedInSession(exchange)
        .flatMap(
            loggedIn ->
                loggedIn
                    ? Mono.error(new OAuth2AuthenticationException(SESSION_AND_AUTHORIZATION))
                    : bearerTokenConverter.convert(exchange));
  }

  /** The repository stores a security context only after a successful login. */
  private static Mono<Boolean> hasLoggedInSession(ServerWebExchange exchange) {
    return exchange
        .getSession()
        .map(
            session ->
                session.getAttribute(
                        WebSessionServerSecurityContextRepository
                            .DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME)
                    != null);
  }
}
