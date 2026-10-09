package lab.libs.identity.testing;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Serves a JWK set over HTTP the way an issuer does, so a service under test can fetch it.
 *
 * <p>Bound to the IPv6 loopback because the Bazel sandbox admits loopback traffic by address and
 * treats a dual-stack socket's mapped IPv4 loopback as foreign.
 */
public final class JwkSetServer implements AutoCloseable {

  private static final String PATH = "/.well-known/jwks.json";

  private final HttpServer server;

  private JwkSetServer(HttpServer server) {
    this.server = server;
  }

  /** Starts on a free port, publishing the given JWK set JSON. */
  public static JwkSetServer start(String jwkSet) {
    try {
      var server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("::1"), 0), 0);
      var body = jwkSet.getBytes(StandardCharsets.UTF_8);
      server.createContext(
          PATH,
          exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var out = exchange.getResponseBody()) {
              out.write(body);
            }
          });
      server.start();
      return new JwkSetServer(server);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String uri() {
    return "http://[::1]:" + server.getAddress().getPort() + PATH;
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
