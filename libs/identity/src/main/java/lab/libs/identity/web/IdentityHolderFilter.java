package lab.libs.identity.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lab.libs.identity.IdentityHolder;
import lab.libs.identity.jwt.IdentityAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exposes the authenticated identity through {@link IdentityHolder} for the rest of the request, so
 * application code needs no Spring Security types to learn who is calling.
 */
public final class IdentityHolderFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!(SecurityContextHolder.getContext().getAuthentication()
        instanceof IdentityAuthentication authentication)) {
      chain.doFilter(request, response);
      return;
    }

    IdentityHolder.set(authentication.identity());
    try {
      chain.doFilter(request, response);
    } finally {
      IdentityHolder.clear();
    }
  }
}
