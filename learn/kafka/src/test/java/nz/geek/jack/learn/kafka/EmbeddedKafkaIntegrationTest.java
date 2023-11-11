package nz.geek.jack.learn.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class EmbeddedKafkaIntegrationTest {

  @Autowired public KafkaTemplate<String, String> template;

  @Autowired private Consumer consumer;

  @Autowired private Producer producer;

  @BeforeEach
  void setup() {
    consumer.resetLatch();
  }

  @Test
  public void givenEmbeddedKafkaBroker_whenSendingWithDefaultTemplate_thenMessageReceived()
      throws Exception {
    String data = "Sending with default template";

    producer.sendMessage("test", data);

    boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);

    assertTrue(messageConsumed);
    assertThat(consumer.getPayload()).contains(data);
  }

  @Test
  public void givenEmbeddedKafkaBroker_whenSendingWithSimpleProducer_thenMessageReceived()
      throws Exception {
    String data = "Sending with our own simple KafkaProducer";

    producer.sendMessage("test", data);

    boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);

    assertTrue(messageConsumed);
    assertThat(consumer.getPayload()).contains(data);
  }
}
