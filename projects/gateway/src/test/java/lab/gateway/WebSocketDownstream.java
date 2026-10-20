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

/**
 * A stub downstream service that counts the requests it receives, accepts every WebSocket upgrade,
 * and records the credentials each handshake carried, so tests can assert what crossed the gateway.
 *
 * <p>Bound to the IPv6 loopback because the Bazel sandbox admits loopback traffic by address and
 * treats a dual-stack socket's mapped IPv4 loopback as foreign.
 */
final class WebSocketDownstream implements AutoCloseable {

  /**
   * The credentials the service received on one upgrade request.
   *
   * @param authorization every {@code Authorization} value.
   * @param cookie every {@code Cookie} value.
   */
  record Handshake(List<String> authorization, List<String> cookie) {}

  private final DisposableServer disposableServer;
  private final AtomicInteger requests;
  private final BlockingQueue<Handshake> handshakes;

  private WebSocketDownstream(
      DisposableServer disposableServer,
      AtomicInteger requests,
      BlockingQueue<Handshake> handshakes) {
    this.disposableServer = disposableServer;
    this.requests = requests;
    this.handshakes = handshakes;
  }

  static WebSocketDownstream start() {
    var requests = new AtomicInteger();
    var handshakes = new LinkedBlockingQueue<Handshake>();

    var disposableServer =
        HttpServer.create()
            .host("::1")
            .port(0)
            .handle(
                (request, response) -> {
                  requests.incrementAndGet();

                  var headers = request.requestHeaders();

                  return response.sendWebsocket(
                      (inbound, outbound) -> {
                        handshakes.add(
                            new Handshake(
                                List.copyOf(headers.getAll(HttpHeaders.AUTHORIZATION)),
                                List.copyOf(headers.getAll(HttpHeaders.COOKIE))));

                        return inbound.receive().then();
                      });
                })
            .bindNow();

    return new WebSocketDownstream(disposableServer, requests, handshakes);
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

  String uri() {
    return "http://[::1]:" + disposableServer.port();
  }

  @Override
  public void close() {
    disposableServer.disposeNow();
  }
}
