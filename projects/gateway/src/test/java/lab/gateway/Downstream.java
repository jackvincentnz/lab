package lab.gateway;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * A stub vertical that answers with its name and the path it received, and reflects the browser
 * credentials it was sent as response headers so tests can assert what crossed the gateway.
 */
final class Downstream implements AutoCloseable {

  static final String COOKIE = "X-Downstream-Cookie";
  static final String AUTHORIZATION = "X-Downstream-Authorization";

  private final DisposableServer server;

  private Downstream(DisposableServer server) {
    this.server = server;
  }

  static Downstream start(String name) {
    var server =
        HttpServer.create()
            .port(0)
            .handle(
                (request, response) -> {
                  request
                      .requestHeaders()
                      .getAll(HttpHeaders.COOKIE)
                      .forEach(cookie -> response.addHeader(COOKIE, cookie));
                  request
                      .requestHeaders()
                      .getAll(HttpHeaders.AUTHORIZATION)
                      .forEach(authorization -> response.addHeader(AUTHORIZATION, authorization));
                  return response.sendString(Mono.just(name + " " + request.uri()));
                })
            .bindNow();
    return new Downstream(server);
  }

  String uri() {
    return "http://localhost:" + server.port();
  }

  @Override
  public void close() {
    server.disposeNow();
  }
}
