package lab.libs.identity.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lab.libs.identity.Identity;
import lab.libs.identity.jwt.IdentityAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests that carry no {@code Authorization} header as a fixed identity, so a
 * vertical can run without an issuer in front of it. A request that does carry a token is left to
 * bearer authentication, so an invalid token is still rejected.
 */
public final class DevelopmentIdentityFilter extends OncePerRequestFilter {

  private final Identity identity;

  public DevelopmentIdentityFilter(Identity identity) {
    this.identity = Objects.requireNonNull(identity, "identity");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
      var context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(new IdentityAuthentication(identity));
      SecurityContextHolder.setContext(context);
    }
    chain.doFilter(request, response);
  }
}
