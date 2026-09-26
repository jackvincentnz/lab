package lab.libs.identity.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lab.libs.identity.Identity;
import lab.libs.identity.IdentityHolder;
import lab.libs.identity.web.IdentityHeaders.Absent;
import lab.libs.identity.web.IdentityHeaders.Invalid;
import lab.libs.identity.web.IdentityHeaders.Present;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Resolves the caller identity from the gateway contract headers before dispatch.
 *
 * <p>Requests to public paths pass through untouched. Every other request must carry a complete,
 * well-formed set of contract headers or it is rejected with 401 before reaching a handler. When a
 * development identity is configured, requests carrying none of the headers are handled as that
 * identity so a vertical can run without a gateway in front of it. Partial or malformed headers are
 * still rejected in that mode.
 */
public class IdentityFilter extends OncePerRequestFilter {

  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final List<String> publicPaths;
  private final Identity developmentIdentity;

  /**
   * @param publicPaths Ant-style path patterns that never require an identity.
   * @param developmentIdentity identity to assume when no contract headers are sent, or {@code
   *     null} to require the headers on every non-public request.
   */
  public IdentityFilter(List<String> publicPaths, Identity developmentIdentity) {
    this.publicPaths = List.copyOf(publicPaths);
    this.developmentIdentity = developmentIdentity;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var result =
        IdentityHeaders.parse(
            request.getHeader(IdentityHeaders.PRINCIPAL_ID),
            request.getHeader(IdentityHeaders.TENANT_ID),
            request.getHeader(IdentityHeaders.SCOPES));

    Identity identity;
    if (result instanceof Present present) {
      identity = present.identity();
    } else if (result instanceof Absent && developmentIdentity != null) {
      identity = developmentIdentity;
    } else {
      reject(response, result instanceof Invalid invalid ? invalid.reason() : "missing identity");
      return;
    }

    IdentityHolder.set(identity);
    try {
      chain.doFilter(request, response);
    } finally {
      IdentityHolder.clear();
    }
  }

  private static void reject(HttpServletResponse response, String reason) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
    response.getWriter().write(reason);
  }
}
