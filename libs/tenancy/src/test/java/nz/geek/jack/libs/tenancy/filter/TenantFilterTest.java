package nz.geek.jack.libs.tenancy.filter;

import static nz.geek.jack.libs.tenancy.filter.TenantFilter.TENANT_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;
import nz.geek.jack.libs.tenancy.TenantContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantFilterTest {

  TenantFilter filter = new TenantFilter();

  @Test
  void doFilter_setsSimpleTenantContext_whenHeaderPresent() throws ServletException, IOException {
    var tenantId = UUID.randomUUID().toString();

    var request = new MockHttpServletRequest();
    request.addHeader(TENANT_ID_HEADER, tenantId);

    var chain = mock(FilterChain.class);

    doAnswer(
            invocation -> {
              assertThat(TenantContextHolder.get().get().getTenantId()).isEqualTo(tenantId);
              return null;
            })
        .when(chain)
        .doFilter(request, null);

    filter.doFilter(request, null, chain);
  }

  @Test
  void doFilter_clearsTenantContext_whenChainCompletes() throws ServletException, IOException {
    var tenantId = UUID.randomUUID().toString();

    var request = new MockHttpServletRequest();
    request.addHeader(TENANT_ID_HEADER, tenantId);

    var chain = mock(FilterChain.class);

    filter.doFilter(request, null, chain);

    assertThat(TenantContextHolder.get()).isEmpty();
  }

  @Test
  void doFilter_succeeds_whenNoHeaderPresent() throws ServletException, IOException {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    filter.doFilter(request, response, chain);
  }
}
