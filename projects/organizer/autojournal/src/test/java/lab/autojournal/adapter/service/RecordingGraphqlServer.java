package lab.autojournal.adapter.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

class RecordingGraphqlServer implements AutoCloseable {

  private final HttpServer server;
  private final AtomicReference<String> requestBody = new AtomicReference<>();
  private String responseBody = "{}";

  RecordingGraphqlServer() throws IOException {
    this.server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    this.server.createContext("/graphql", this::handleGraphql);
    this.server.start();
  }

  String graphqlUrl() {
    return String.format("http://localhost:%s/graphql", server.getAddress().getPort());
  }

  void respondWith(String responseBody) {
    this.responseBody = responseBody;
  }

  String requestBody() {
    return requestBody.get();
  }

  private void handleGraphql(HttpExchange exchange) throws IOException {
    try (exchange) {
      requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      var bytes = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
    }
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
