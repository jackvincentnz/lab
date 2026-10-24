package lab.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Refuses a bearer token sent with a live session, so neither credential silently wins. */
class SingleCredentialBearerTokenConverter implements ServerAuthenticationConverter {

  private static final BearerTokenError SESSION_AND_BEARER =
      new BearerTokenError(
          BearerTokenErrorCodes.INVALID_REQUEST,
          HttpStatus.BAD_REQUEST,
          "Send a session or a bearer token, not both",
          "https://tools.ietf.org/html/rfc6750#section-3.1");

  private final ServerAuthenticationConverter bearerTokenConverter =
      new ServerBearerTokenAuthenticationConverter();

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    return bearerTokenConverter
        .convert(exchange)
        .flatMap(
            token ->
                hasAuthenticatedSession(exchange)
                    .flatMap(
                        both ->
                            both
                                ? Mono.error(new OAuth2AuthenticationException(SESSION_AND_BEARER))
                                : Mono.just(token)));
  }

  private static Mono<Boolean> hasAuthenticatedSession(ServerWebExchange exchange) {
    return exchange
        .getSession()
        .map(
            session -> {
              SecurityContext context =
                  session.getAttribute(
                      WebSessionServerSecurityContextRepository
                          .DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME);
              return context != null
                  && context.getAuthentication() != null
                  && context.getAuthentication().isAuthenticated();
            });
  }
}
