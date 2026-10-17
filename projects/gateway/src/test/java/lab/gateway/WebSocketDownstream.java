package lab.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpHeaders;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;

/**
 * A stub downstream service that accepts WebSocket upgrades, records each handshake it receives,
 * and answers every text message with {@code "echo "} and the message, so tests can assert what
 * crossed the gateway in both directions.
 *
 * <p>Bound to the IPv6 loopback because the Bazel sandbox admits loopback traffic by address and
 * treats a dual-stack socket's mapped IPv4 loopback as foreign.
 */
final class WebSocketDownstream implements AutoCloseable {

  /** The subprotocol GraphQL clients request, which this service also speaks. */
  static final String SUBPROTOCOL = "graphql-transport-ws";

  /**
   * What the service received on one upgrade request.
   *
   * @param path the request path and query.
   * @param authorization every {@code Authorization} value.
   * @param cookie every {@code Cookie} value.
   * @param host the {@code Host} value.
   * @param requestId the {@code X-Request-ID} value.
   * @param subprotocol the subprotocol the service selected.
   */
  record Handshake(
      String path,
      List<String> authorization,
      List<String> cookie,
      String host,
      String requestId,
      String subprotocol) {}

  private final DisposableServer server;
  private final AtomicInteger requests;
  private final BlockingQueue<Handshake> handshakes;

  private WebSocketDownstream(
      DisposableServer server, AtomicInteger requests, BlockingQueue<Handshake> handshakes) {
    this.server = server;
    this.requests = requests;
    this.handshakes = handshakes;
  }

  static WebSocketDownstream start() {
    var requests = new AtomicInteger();
    var handshakes = new LinkedBlockingQueue<Handshake>();
    var server =
        HttpServer.create()
            .host("::1")
            .port(0)
            .handle(
                (request, response) -> {
                  requests.incrementAndGet();
                  var headers = request.requestHeaders();
                  return response.sendWebsocket(
                      (in, out) -> {
                        handshakes.add(
                            new Handshake(
                                request.uri(),
                                List.copyOf(headers.getAll(HttpHeaders.AUTHORIZATION)),
                                List.copyOf(headers.getAll(HttpHeaders.COOKIE)),
                                headers.get(HttpHeaders.HOST),
                                headers.get(AccessLogWebFilter.REQUEST_ID),
                                in.selectedSubprotocol()));
                        return out.sendString(in.receive().asString().map(text -> "echo " + text));
                      },
                      WebsocketServerSpec.builder().protocols(SUBPROTOCOL).build());
                })
            .bindNow();
    return new WebSocketDownstream(server, requests, handshakes);
  }

  /** How many requests crossed the gateway to this service, upgraded or not. */
  int requests() {
    return requests.get();
  }

  /** The next handshake the service accepted, waiting briefly for it to arrive. */
  Handshake handshake() throws InterruptedException {
    var handshake = handshakes.poll(10, TimeUnit.SECONDS);
    assertThat(handshake).as("handshake that reached the service").isNotNull();
    return handshake;
  }

  int port() {
    return server.port();
  }

  String uri() {
    return "http://[::1]:" + port();
  }

  @Override
  public void close() {
    server.disposeNow();
  }
}
