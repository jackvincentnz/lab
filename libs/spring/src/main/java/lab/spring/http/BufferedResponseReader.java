package lab.spring.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

public class BufferedResponseReader implements ClientHttpResponse {

  private final ClientHttpResponse delegate;
  private final byte[] body;

  public BufferedResponseReader(ClientHttpResponse delegate) throws IOException {
    this.delegate = delegate;
    this.body = toByteArray(delegate.getBody());
  }

  private byte[] toByteArray(InputStream inputStream) throws IOException {
    var buffer = new ByteArrayOutputStream();
    var tmp = new byte[1024];
    int n;
    while ((n = inputStream.read(tmp)) != -1) {
      buffer.write(tmp, 0, n);
    }
    return buffer.toByteArray();
  }

  @Override
  public HttpStatusCode getStatusCode() throws IOException {
    return delegate.getStatusCode();
  }

  @Override
  public String getStatusText() throws IOException {
    return delegate.getStatusText();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public InputStream getBody() {
    return new ByteArrayInputStream(body);
  }

  @Override
  public HttpHeaders getHeaders() {
    return delegate.getHeaders();
  }

  public String getBodyAsString() {
    return new String(body);
  }
}
