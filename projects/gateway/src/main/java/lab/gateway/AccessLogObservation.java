package lab.gateway;

import io.micrometer.common.KeyValue;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.web.server.ServerWebExchange;

/**
 * Names the access record fields that filters learn along the way and adds them to the request's
 * observation, which {@link AccessLogHandler} writes as the access record when the observation
 * stops.
 *
 * <p>Fields are high-cardinality key values only, so they never become tags on the {@code
 * http.server.requests} metric.
 */
final class AccessLogObservation {

  static final String REQUEST_ID = "request_id";
  static final String TENANT_ID = "tenant_id";
  static final String PRINCIPAL_ID = "principal_id";
  static final String AUTHENTICATION_METHOD = "authentication_method";

  private AccessLogObservation() {}

  static void add(ServerWebExchange exchange, String key, String value) {
    // HttpWebHandlerAdapter stores the request observation's context in the exchange attributes
    // before any WebFilter runs.
    ServerRequestObservationContext.findCurrent(exchange.getAttributes())
        .ifPresent(context -> context.addHighCardinalityKeyValue(KeyValue.of(key, value)));
  }
}
