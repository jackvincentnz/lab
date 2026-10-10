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
 * response is complete, from the request, the response, and the fields {@link AccessLogObservation}
 * added along the way.
 *
 * <p>The record is built from named fields only, so no header, cookie, token, or request body value
 * can reach the log. A field is left out, rather than written empty, when it is unknown.
 */
@Component
class AccessLogHandler implements ObservationHandler<ServerRequestObservationContext> {

  private static final Logger log = LoggerFactory.getLogger(AccessLogHandler.class);

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
    var record =
        log.atInfo()
            .addKeyValue("method", request.getMethod().name())
            .addKeyValue("path", request.getPath().value());
    var status = context.getResponse() == null ? null : context.getResponse().getStatusCode();
    // An aborted request has no status the client received.
    if (status != null && !context.isConnectionAborted()) {
      record = record.addKeyValue("status", status.value());
    }
    Long started = context.get(STARTED);
    if (started != null) {
      record =
          record.addKeyValue(
              "duration_ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
    }
    record = addIfKnown(record, "source_ip", sourceIp(request));
    for (var field : FIELDS) {
      KeyValue value = context.getHighCardinalityKeyValue(field);
      record = addIfKnown(record, field, value == null ? null : value.getValue());
    }
    record.log("Access");
  }

  private static LoggingEventBuilder addIfKnown(
      LoggingEventBuilder record, String key, String value) {
    return value == null ? record : record.addKeyValue(key, value);
  }

  /** The socket peer; forwarded headers are not trusted here. */
  private static String sourceIp(ServerHttpRequest request) {
    InetSocketAddress remote = request.getRemoteAddress();
    if (remote == null) {
      return null;
    }
    return remote.getAddress() == null
        ? remote.getHostString()
        : remote.getAddress().getHostAddress();
  }
}
