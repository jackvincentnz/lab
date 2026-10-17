package lab.libs.messaging;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** The Kafka header that identifies an event, shared so producers and consumers agree on it. */
public final class EventIdHeader {

  public static final String NAME = "event-id";

  private EventIdHeader() {}

  public static byte[] toBytes(UUID eventId) {
    return eventId.toString().getBytes(StandardCharsets.UTF_8);
  }

  public static UUID fromBytes(byte[] value) {
    return UUID.fromString(new String(value, StandardCharsets.UTF_8));
  }
}
