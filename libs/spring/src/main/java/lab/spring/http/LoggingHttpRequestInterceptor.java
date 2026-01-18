package lab.spring.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingHttpRequestInterceptor.class);

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    logRequest(request, body);

    var response = execution.execute(request, body);

    if (!LOGGER.isTraceEnabled()) {
      return response;
    }

    var bufferedResponse = new BufferedResponseReader(response);
    logResponse(bufferedResponse);

    return bufferedResponse;
  }

  private void logRequest(HttpRequest request, byte[] body) {
    var message =
        new StringBuilder()
            .append("Request URI: ")
            .append(request.getURI())
            .append(", Method: ")
            .append(request.getMethod());

    if (body.length > 0) {
      message.append(", Body: ").append(new String(body, StandardCharsets.UTF_8));
    }

    LOGGER.trace(message.toString());
  }

  private void logResponse(BufferedResponseReader response) throws IOException {
    LOGGER.trace(
        "Response Status: {}, Body: {}",
        response.getStatusCode(),
        response.getBodyAsString().trim());
  }
}
