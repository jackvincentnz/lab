package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.Optional;
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
  void convert_withBearerAndLoggedInSession_failsAsInvalidRequest() {
    var exchange = exchange(Optional.of("Bearer " + randomString()), loggedInSession());

    var thrown =
        catchThrowableOfType(
            OAuth2AuthenticationException.class, () -> converter.convert(exchange).block());

    assertThat(thrown.getError().getErrorCode()).isEqualTo(BearerTokenErrorCodes.INVALID_REQUEST);
  }

  @Test
  void convert_withBasicAuthorizationAndLoggedInSession_failsAsInvalidRequest() {
    var exchange = exchange(Optional.of("Basic " + randomString()), loggedInSession());

    var thrown =
        catchThrowableOfType(
            OAuth2AuthenticationException.class, () -> converter.convert(exchange).block());

    assertThat(thrown.getError().getErrorCode()).isEqualTo(BearerTokenErrorCodes.INVALID_REQUEST);
  }

  @Test
  void convert_withBearerAndEmptySession_returnsTheBearerToken() {
    var token = randomString();
    var exchange = exchange(Optional.of("Bearer " + token), new MockWebSession());

    var authentication = converter.convert(exchange).block();

    assertThat(authentication)
        .isInstanceOfSatisfying(
            BearerTokenAuthenticationToken.class,
            bearer -> assertThat(bearer.getToken()).isEqualTo(token));
  }

  @Test
  void convert_withoutBearer_isEmpty() {
    var exchange = exchange(Optional.empty(), loggedInSession());

    var authentication = converter.convert(exchange).blockOptional();

    assertThat(authentication).isEmpty();
  }

  private static MockServerWebExchange exchange(
      Optional<String> authorization, MockWebSession session) {
    var request = MockServerHttpRequest.get("/");
    authorization.ifPresent(value -> request.header(HttpHeaders.AUTHORIZATION, value));
    return MockServerWebExchange.builder(request.build()).session(session).build();
  }

  private MockWebSession loggedInSession() {
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
