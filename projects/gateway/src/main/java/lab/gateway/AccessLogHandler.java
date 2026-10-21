package lab.gateway;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;

/**
 * Writes one access record for each request when the request's observation stops, after the
 * response is complete, from the request, the response, and the fields {@link RequestIdWebFilter}
 * and {@link CallerWebFilter} added along the way.
 *
 * <p>The record is built from named fields only, so no header, cookie, token, or request body value
 * can reach the log. A field is left out, rather than written empty, when it is unknown.
 */
@Component
class AccessLogHandler implements ObservationHandler<ServerRequestObservationContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogHandler.class);

  private static final String STARTED = AccessLogHandler.class.getName() + ".started";

  private static final String[] FIELDS = {
    AccessLogObservation.REQUEST_ID,
    AccessLogObservation.TENANT_ID,
    AccessLogObservation.PRINCIPAL_ID,
    AccessLogObservation.AUTHENTICATION_METHOD,
  };

  @Override
  public boolean supportsContext(Observation.Context context) {
    return context instanceof ServerRequestObservationContext;
  }

  @Override
  public void onStart(ServerRequestObservationContext context) {
    context.put(STARTED, System.nanoTime());
  }

  @Override
  public void onStop(ServerRequestObservationContext context) {
    // The carrier is the request as it arrived, before routes rewrite its path.
    var request = context.getCarrier();
    var method = request.getMethod().name();
    var path = request.getPath().value();
    var status = status(context);
    long started = context.getRequired(STARTED);
    var durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

    var loggingEventBuilder =
        LOGGER.atInfo().addKeyValue("method", method).addKeyValue("path", path);
    if (status != null) {
      loggingEventBuilder = loggingEventBuilder.addKeyValue("status", status);
    }
    loggingEventBuilder = loggingEventBuilder.addKeyValue("duration_ms", durationMs);

    loggingEventBuilder = addIfKnown(loggingEventBuilder, "source_ip", sourceIp(request));
    for (var field : FIELDS) {
      KeyValue keyValue = context.getHighCardinalityKeyValue(field);
      loggingEventBuilder =
          addIfKnown(loggingEventBuilder, field, keyValue == null ? null : keyValue.getValue());
    }

    // The message repeats the fields a plain-text console needs, since it prints no key values.
    loggingEventBuilder.log(
        "{} {} {} {}ms", method, path, status == null ? "-" : status, durationMs);
  }

  /** The status the client received, or null when it received none. */
  private static Integer status(ServerRequestObservationContext context) {
    var response = context.getResponse();
    var status = response.getStatusCode();
    // As in Spring's request observation convention, a client that disconnected before the
    // response was committed received no status, and one that disconnected after it did.
    var statusUnknown = context.isConnectionAborted() && !response.isCommitted();
    return status == null || statusUnknown ? null : status.value();
  }

  private static LoggingEventBuilder addIfKnown(
      LoggingEventBuilder loggingEventBuilder, String key, String value) {
    return value == null ? loggingEventBuilder : loggingEventBuilder.addKeyValue(key, value);
  }

  /** The remote address as the server reports it, which Spring Boot derives from the platform. */
  private static String sourceIp(ServerHttpRequest request) {
    InetSocketAddress remoteAddress = request.getRemoteAddress();
    if (remoteAddress == null) {
      return null;
    }

    return remoteAddress.isUnresolved()
        ? remoteAddress.getHostString()
        : remoteAddress.getAddress().getHostAddress();
  }
}
