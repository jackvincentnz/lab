package lab.libs.identity.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lab.libs.identity.Identity;
import lab.libs.identity.IdentityHolder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class IdentityFilterTest {

  private static final List<String> PUBLIC_PATHS = List.of("/actuator/health/**");
  private static final Identity DEVELOPMENT =
      new Identity(UUID.randomUUID(), UUID.randomUUID(), Set.of("mops:read"));

  private final IdentityFilter filter = new IdentityFilter(PUBLIC_PATHS, null);
  private final IdentityFilter developmentFilter = new IdentityFilter(PUBLIC_PATHS, DEVELOPMENT);

  @Test
  void doFilter_exposesIdentityToChain_andClearsAfterwards() throws ServletException, IOException {
    var principalId = UUID.randomUUID();
    var tenantId = UUID.randomUUID();
    var request = new MockHttpServletRequest("POST", "/graphql");
    request.addHeader(IdentityHeaders.PRINCIPAL_ID, principalId.toString());
    request.addHeader(IdentityHeaders.TENANT_ID, tenantId.toString());
    request.addHeader(IdentityHeaders.SCOPES, "mops:read mops:write");
    var seen = new AtomicReference<Identity>();
    var chain = new MockFilterChain(new CapturingServlet(seen));

    filter.doFilter(request, new MockHttpServletResponse(), chain);

    assertThat(seen.get())
        .isEqualTo(new Identity(principalId, tenantId, Set.of("mops:read", "mops:write")));
    assertThat(IdentityHolder.get()).isEmpty();
  }

  @Test
  void doFilter_rejectsWith401_whenHeadersAbsent() throws ServletException, IOException {
    var request = new MockHttpServletRequest("POST", "/graphql");
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).isEqualTo("missing identity");
    assertThat(chain.getRequest()).isNull();
  }

  @Test
  void doFilter_rejectsWith401_whenHeadersMalformed() throws ServletException, IOException {
    var request = new MockHttpServletRequest("POST", "/graphql");
    request.addHeader(IdentityHeaders.PRINCIPAL_ID, "nope");
    request.addHeader(IdentityHeaders.TENANT_ID, UUID.randomUUID().toString());
    request.addHeader(IdentityHeaders.SCOPES, "mops:read");
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).isEqualTo("malformed X-Principal-Id");
    assertThat(chain.getRequest()).isNull();
  }

  @Test
  void doFilter_skipsPublicPaths() throws ServletException, IOException {
    var request = new MockHttpServletRequest("GET", "/actuator/health/liveness");
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(chain.getRequest()).isSameAs(request);
  }

  @Test
  void doFilter_usesDevelopmentIdentity_whenHeadersAbsent() throws ServletException, IOException {
    var request = new MockHttpServletRequest("POST", "/graphql");
    var seen = new AtomicReference<Identity>();
    var chain = new MockFilterChain(new CapturingServlet(seen));

    developmentFilter.doFilter(request, new MockHttpServletResponse(), chain);

    assertThat(seen.get()).isEqualTo(DEVELOPMENT);
  }

  @Test
  void doFilter_stillRejectsMalformedHeaders_inDevelopmentMode()
      throws ServletException, IOException {
    var request = new MockHttpServletRequest("POST", "/graphql");
    request.addHeader(IdentityHeaders.PRINCIPAL_ID, UUID.randomUUID().toString());
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    developmentFilter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).isEqualTo("missing X-Tenant-Id");
    assertThat(chain.getRequest()).isNull();
  }

  private static final class CapturingServlet extends jakarta.servlet.http.HttpServlet {

    private final AtomicReference<Identity> seen;

    private CapturingServlet(AtomicReference<Identity> seen) {
      this.seen = seen;
    }

    @Override
    protected void service(
        jakarta.servlet.http.HttpServletRequest request,
        jakarta.servlet.http.HttpServletResponse response) {
      seen.set(IdentityHolder.get().orElse(null));
    }
  }
}
