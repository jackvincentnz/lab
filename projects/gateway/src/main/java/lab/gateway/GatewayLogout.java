package lab.gateway;

import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.LogoutWebFilter;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.csrf.CsrfServerLogoutHandler;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;

/**
 * Logout that ends with the session invalidated, so a logged-out cookie can never be reused.
 *
 * <p>Spring's logout spec appends its CSRF handler after any handler it is given, and that handler
 * rotates the session id, which fails once the session is invalidated. Wiring the filter directly
 * keeps Spring's handlers but puts invalidation last.
 */
final class GatewayLogout {
  private GatewayLogout() {}

  static LogoutWebFilter filter(ServerCsrfTokenRepository csrfTokens) {
    var filter = new LogoutWebFilter();
    filter.setLogoutHandler(
        new DelegatingServerLogoutHandler(
            AccessLogObservation.callerOnLogout(),
            new SecurityContextServerLogoutHandler(),
            new CsrfServerLogoutHandler(csrfTokens),
            new WebSessionServerLogoutHandler()));
    return filter;
  }
}
