package lab.spring.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

public class BufferedResponseReaderTest {

  private static final String CONTENT = "test";

  private ClientHttpResponse mockResponse;

  private BufferedResponseReader reader;

  @BeforeEach
  public void setup() throws IOException {
    mockResponse = mock(ClientHttpResponse.class);
    when(mockResponse.getBody()).thenReturn(new ByteArrayInputStream(CONTENT.getBytes()));
    reader = new BufferedResponseReader(mockResponse);
  }

  @Test
  void getStatusCode_delegates() throws IOException {
    when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);

    reader.getStatusCode();

    verify(mockResponse).getStatusCode();
  }

  @Test
  void getStatusText_delegates() throws IOException {
    when(mockResponse.getStatusText()).thenReturn("OK");

    reader.getStatusText();

    verify(mockResponse).getStatusText();
  }

  @Test
  void close_delegates() {
    reader.close();

    verify(mockResponse).close();
  }

  @Test
  void getBody_returnsBody() {
    var body = reader.getBody();

    assertThat(body).isNotNull();
  }

  @Test
  void getBody_canBeCalledTwice() throws IOException {
    var firstCall = reader.getBody();
    var secondCall = reader.getBody();

    assertThat(new String(firstCall.readAllBytes()))
        .isEqualTo(new String(secondCall.readAllBytes()));
  }

  @Test
  void getHeaders_delegates() {
    var headers = new HttpHeaders();
    when(mockResponse.getHeaders()).thenReturn(headers);

    reader.getHeaders();

    verify(mockResponse).getHeaders();
  }

  @Test
  void getBodyAsString_returnsBody() {
    var body = reader.getBodyAsString();

    assertThat(body).isEqualTo(CONTENT);
  }
}
