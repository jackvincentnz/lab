package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

class GatewayLogoutTest extends TestBase {

  ServerCsrfTokenRepository csrfTokens = mock(ServerCsrfTokenRepository.class);
  WebSession session = mock(WebSession.class);
  WebFilterChain chain = mock(WebFilterChain.class);
  Map<String, Object> attributes = new HashMap<>();
  AtomicBoolean continued = new AtomicBoolean();

  GatewayLogoutTest() {
    when(session.getAttributes()).thenReturn(attributes);
    when(session.changeSessionId()).thenReturn(Mono.empty());
    when(session.invalidate()).thenReturn(Mono.empty());
    when(csrfTokens.saveToken(any(), isNull())).thenReturn(Mono.empty());
    // Spring builds the continuation eagerly, so track whether it actually runs.
    when(chain.filter(any())).thenReturn(Mono.fromRunnable(() -> continued.set(true)));
  }

  @Test
  void logout_clearsIdentityAndTokenBeforeInvalidatingSession() {
    var attribute =
        WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;
    attributes.put(attribute, randomString());
    var exchange = logoutExchange();

    GatewayLogout.filter(csrfTokens).filter(exchange, chain).block();

    assertThat(attributes).doesNotContainKey(attribute);
    var order = inOrder(session, csrfTokens);
    order.verify(session).changeSessionId();
    order.verify(csrfTokens).saveToken(exchange, null);
    order.verify(session).invalidate();
    verify(session, times(1)).invalidate();
  }

  @Test
  void logout_redirectsToLoginPage() {
    var exchange = logoutExchange();

    GatewayLogout.filter(csrfTokens).filter(exchange, chain).block();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(exchange.getResponse().getHeaders().getLocation())
        .hasPath("/login")
        .hasQuery("logout");
    assertThat(continued).isFalse();
  }

  @Test
  void otherRequests_passThrough() {
    var exchange =
        MockServerWebExchange.builder(MockServerHttpRequest.get("/logout"))
            .session(session)
            .build();

    GatewayLogout.filter(csrfTokens).filter(exchange, chain).block();

    assertThat(continued).isTrue();
    verify(session, never()).invalidate();
  }

  private MockServerWebExchange logoutExchange() {
    return MockServerWebExchange.builder(MockServerHttpRequest.post("/logout"))
        .session(session)
        .build();
  }
}
