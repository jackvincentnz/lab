package lab.libs.identity.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lab.libs.identity.Identity;
import lab.libs.identity.IdentityHolder;
import lab.libs.identity.jwt.IdentityAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class IdentityHolderFilterTest {

  private static final Identity IDENTITY =
      new Identity(UUID.randomUUID(), UUID.randomUUID(), Set.of("mops:read"));

  private final IdentityHolderFilter filter = new IdentityHolderFilter();

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
    IdentityHolder.clear();
  }

  @Test
  void doFilter_exposesAuthenticatedIdentity_andClearsAfterwards()
      throws ServletException, IOException {
    SecurityContextHolder.getContext().setAuthentication(new IdentityAuthentication(IDENTITY));
    var seen = new AtomicReference<Identity>();

    filter.doFilter(
        new MockHttpServletRequest(),
        new MockHttpServletResponse(),
        new MockFilterChain(new CapturingServlet(seen)));

    assertThat(seen.get()).isEqualTo(IDENTITY);
    assertThat(IdentityHolder.get()).isEmpty();
  }

  @Test
  void doFilter_leavesHolderEmpty_whenRequestIsNotAuthenticated()
      throws ServletException, IOException {
    var seen = new AtomicReference<Identity>();
    var chain = new MockFilterChain(new CapturingServlet(seen));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(seen.get()).isNull();
    assertThat(chain.getRequest()).isNotNull();
  }

  private static final class CapturingServlet extends HttpServlet {

    private final AtomicReference<Identity> seen;

    private CapturingServlet(AtomicReference<Identity> seen) {
      this.seen = seen;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
      seen.set(IdentityHolder.get().orElse(null));
    }
  }
}
