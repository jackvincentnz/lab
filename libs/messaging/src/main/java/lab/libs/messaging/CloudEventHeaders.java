package lab.libs.messaging;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;

/**
 * Writes and reads event metadata as Kafka headers named by the CloudEvents Kafka binding, so every
 * message type carries it without a field in its schema.
 */
public final class CloudEventHeaders {

  public static final String ID = "ce_id";

  public static final String TYPE = "ce_type";

  public static final String SOURCE = "ce_source";

  private CloudEventHeaders() {}

  public static void set(Headers headers, UUID id, String type, String source) {
    headers.add(ID, bytes(id.toString()));
    headers.add(TYPE, bytes(type));
    headers.add(SOURCE, bytes(source));
  }

  public static Optional<UUID> readId(Headers headers) {
    var header = headers.lastHeader(ID);
    return header == null ? Optional.empty() : parseId(header.value());
  }

  /*
   * Messages published before ids existed have no header, and a malformed one is no more useful to
   * a consumer. Both read as empty so that one bad message does not stop a listener.
   */
  public static Optional<UUID> parseId(byte[] value) {
    if (value == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(UUID.fromString(new String(value, StandardCharsets.UTF_8)));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private static byte[] bytes(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }
}
