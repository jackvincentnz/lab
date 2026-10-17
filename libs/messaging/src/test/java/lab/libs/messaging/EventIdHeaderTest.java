package lab.libs.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class EventIdHeaderTest extends TestBase {

  @Test
  void fromBytes_bytesOfEventId_returnsEventId() {
    var eventId = UUID.randomUUID();

    assertThat(EventIdHeader.fromBytes(EventIdHeader.toBytes(eventId))).isEqualTo(eventId);
  }
}
