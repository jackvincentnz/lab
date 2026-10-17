package lab.libs.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lab.test.TestBase;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;

class CloudEventHeadersTest extends TestBase {

  @Test
  void set_metadata_writesCloudEventHeaders() {
    var headers = new RecordHeaders();
    var id = UUID.randomUUID();
    var type = randomString();
    var source = randomString();

    CloudEventHeaders.set(headers, id, type, source);

    assertThat(value(headers, CloudEventHeaders.ID)).isEqualTo(id.toString());
    assertThat(value(headers, CloudEventHeaders.TYPE)).isEqualTo(type);
    assertThat(value(headers, CloudEventHeaders.SOURCE)).isEqualTo(source);
  }

  @Test
  void readId_idSet_returnsId() {
    var headers = new RecordHeaders();
    var id = UUID.randomUUID();
    CloudEventHeaders.set(headers, id, randomString(), randomString());

    assertThat(CloudEventHeaders.readId(headers)).contains(id);
  }

  @Test
  void readId_noIdHeader_returnsEmpty() {
    assertThat(CloudEventHeaders.readId(new RecordHeaders())).isEmpty();
  }

  @Test
  void parseId_malformed_returnsEmpty() {
    var value = randomString().getBytes(StandardCharsets.UTF_8);

    assertThat(CloudEventHeaders.parseId(value)).isEmpty();
  }

  @Test
  void parseId_null_returnsEmpty() {
    assertThat(CloudEventHeaders.parseId(null)).isEmpty();
  }

  private static String value(RecordHeaders headers, String key) {
    return new String(headers.lastHeader(key).value(), StandardCharsets.UTF_8);
  }
}
