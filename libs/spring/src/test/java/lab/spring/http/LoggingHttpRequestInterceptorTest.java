package lab.spring.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lab.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class LoggingHttpRequestInterceptorTest extends TestBase {

  private final Logger logger =
      (Logger) LoggerFactory.getLogger(LoggingHttpRequestInterceptor.class);

  private final Level originalLevel = logger.getLevel();

  private final LoggingHttpRequestInterceptor interceptor = new LoggingHttpRequestInterceptor();

  // The logger is shared by every test in the JVM, so each test restores the level it set.
  @AfterEach
  void restoreLevel() {
    logger.setLevel(originalLevel);
  }

  @Test
  void intercept_traceDisabled_executesRequestWithSameBody() throws IOException {
    logger.setLevel(Level.DEBUG);
    var request = request();
    var body = randomString().getBytes(StandardCharsets.UTF_8);
    var response = response(randomString());
    var execution = mock(ClientHttpRequestExecution.class);
    when(execution.execute(request, body)).thenReturn(response);

    interceptor.intercept(request, body, execution);

    verify(execution).execute(request, body);
  }

  @Test
  void intercept_traceDisabled_returnsOriginalResponse() throws IOException {
    logger.setLevel(Level.DEBUG);
    var request = request();
    var body = randomString().getBytes(StandardCharsets.UTF_8);
    var response = response(randomString());
    var execution = mock(ClientHttpRequestExecution.class);
    when(execution.execute(request, body)).thenReturn(response);

    var result = interceptor.intercept(request, body, execution);

    assertThat(result).isSameAs(response);
  }

  @Test
  void intercept_traceEnabled_executesRequestWithSameBody() throws IOException {
    logger.setLevel(Level.TRACE);
    var request = request();
    var body = randomString().getBytes(StandardCharsets.UTF_8);
    var response = response(randomString());
    var execution = mock(ClientHttpRequestExecution.class);
    when(execution.execute(request, body)).thenReturn(response);

    interceptor.intercept(request, body, execution);

    verify(execution).execute(request, body);
  }

  @Test
  void intercept_traceEnabled_returnsBufferedResponseWithReadableBody() throws IOException {
    logger.setLevel(Level.TRACE);
    var request = request();
    var body = randomString().getBytes(StandardCharsets.UTF_8);
    var content = randomString();
    var response = response(content);
    var execution = mock(ClientHttpRequestExecution.class);
    when(execution.execute(request, body)).thenReturn(response);

    var result = interceptor.intercept(request, body, execution);

    assertThat(result).isInstanceOf(BufferedResponseReader.class);
    assertThat(new String(result.getBody().readAllBytes(), StandardCharsets.UTF_8))
        .isEqualTo(content);
  }

  private HttpRequest request() {
    var request = mock(HttpRequest.class);
    when(request.getURI()).thenReturn(URI.create("http://localhost/" + randomString()));
    when(request.getMethod()).thenReturn(HttpMethod.POST);
    return request;
  }

  private ClientHttpResponse response(String content) throws IOException {
    var response = mock(ClientHttpResponse.class);
    when(response.getStatusCode()).thenReturn(HttpStatus.OK);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    return response;
  }
}
