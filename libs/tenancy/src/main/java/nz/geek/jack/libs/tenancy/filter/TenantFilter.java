package nz.geek.jack.libs.tenancy.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import nz.geek.jack.libs.tenancy.SimpleTenantContext;
import nz.geek.jack.libs.tenancy.TenantContextHolder;

public class TenantFilter implements Filter {

  protected static final String TENANT_ID_HEADER = "tenant-id";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    HttpServletRequest req = (HttpServletRequest) request;

    String tenantId = req.getHeader(TENANT_ID_HEADER);
    if (tenantId != null) {
      TenantContextHolder.set(SimpleTenantContext.of(tenantId));
    }

    try {
      chain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear();
    }
  }
}
