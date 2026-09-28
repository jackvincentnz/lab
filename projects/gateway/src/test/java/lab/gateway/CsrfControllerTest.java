package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.DefaultCsrfToken;
import reactor.core.publisher.Mono;

class CsrfControllerTest extends TestBase {

  CsrfController controller = new CsrfController();

  @Test
  void csrf_returnsTheTokenSpringAttachedToTheExchange() {
    var token = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", randomString());
    var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/csrf"));
    exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(token));

    var result = controller.csrf(exchange).block();

    assertThat(result).isSameAs(token);
  }

  @Test
  void csrf_failsWhenNoTokenWasAttached() {
    var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/csrf"));

    assertThatIllegalArgumentException().isThrownBy(() -> controller.csrf(exchange));
  }
}
