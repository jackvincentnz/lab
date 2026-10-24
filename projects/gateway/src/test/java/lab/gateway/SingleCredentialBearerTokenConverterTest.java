package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.mock.web.server.MockWebSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

class SingleCredentialBearerTokenConverterTest extends TestBase {

  private final SingleCredentialBearerTokenConverter converter =
      new SingleCredentialBearerTokenConverter();

  @Test
  void convert_withBearerAndAuthenticatedSession_failsAsInvalidRequest() {
    var exchange = exchange(randomString(), authenticatedSession());

    var thrown =
        catchThrowableOfType(
            OAuth2AuthenticationException.class, () -> converter.convert(exchange).block());

    assertThat(thrown.getError().getErrorCode()).isEqualTo(BearerTokenErrorCodes.INVALID_REQUEST);
  }

  @Test
  void convert_withBearerAndUnauthenticatedSession_returnsTheBearerToken() {
    var token = randomString();

    var authentication = converter.convert(exchange(token, new MockWebSession())).block();

    assertThat(authentication)
        .isInstanceOfSatisfying(
            BearerTokenAuthenticationToken.class,
            bearer -> assertThat(bearer.getToken()).isEqualTo(token));
  }

  @Test
  void convert_withoutBearer_isEmpty() {
    var request = MockServerHttpRequest.get("/").build();
    var exchange = MockServerWebExchange.builder(request).session(authenticatedSession()).build();

    assertThat(converter.convert(exchange).blockOptional()).isEmpty();
  }

  private static MockServerWebExchange exchange(String token, MockWebSession session) {
    var request =
        MockServerHttpRequest.get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
    return MockServerWebExchange.builder(request).session(session).build();
  }

  private MockWebSession authenticatedSession() {
    var session = new MockWebSession();
    var authentication =
        UsernamePasswordAuthenticationToken.authenticated(randomString(), null, null);
    session
        .getAttributes()
        .put(
            WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME,
            new SecurityContextImpl(authentication));
    return session;
  }
}
