package lab.libs.identity.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import lab.libs.identity.Identity;
import lab.libs.identity.jwt.IdentityAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class DevelopmentIdentityFilterTest {

  private static final Identity DEVELOPMENT =
      new Identity(UUID.randomUUID(), UUID.randomUUID(), Set.of("mops:read"));

  private final DevelopmentIdentityFilter filter = new DevelopmentIdentityFilter(DEVELOPMENT);

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilter_authenticatesAsDevelopmentIdentity_whenNoAuthorizationHeader()
      throws ServletException, IOException {
    filter.doFilter(
        new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isInstanceOf(IdentityAuthentication.class);
    assertThat(((IdentityAuthentication) authentication).identity()).isEqualTo(DEVELOPMENT);
  }

  @Test
  void doFilter_leavesRequestToBearerAuthentication_whenAuthorizationHeaderPresent()
      throws ServletException, IOException {
    var request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer nope");

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
