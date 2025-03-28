package nz.geek.jack.learn.kafka;

import static nz.geek.jack.learn.kafka.Producer.TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class EmbeddedKafkaIntegrationTest {

  @MockitoBean private MessageHandler mockHandler;

  @Autowired public KafkaTemplate<String, SimpleMessage> template;

  @Autowired private Producer producer;

  @Test
  public void givenEmbeddedKafkaBroker_whenSendingWithKafkaTemplate_thenMessageReceived() {
    var message = SimpleMessage.newBuilder().setContent("Sending with kafka template").build();
    var messageCaptor = ArgumentCaptor.forClass(SimpleMessage.class);

    template.send(TOPIC, "test", message);

    verify(mockHandler, timeout(10000))
        .handle(messageCaptor.capture(), any(String.class), any(String.class));

    assertThat(messageCaptor.getValue()).isEqualTo(message);
  }

  @Test
  public void givenEmbeddedKafkaBroker_whenSendingWithProducer_thenMessageReceived() {
    var content = "Sending with producer";
    var messageCaptor = ArgumentCaptor.forClass(SimpleMessage.class);

    producer.sendMessage("test", content);

    verify(mockHandler, timeout(10000))
        .handle(messageCaptor.capture(), any(String.class), any(String.class));

    assertThat(messageCaptor.getValue().getContent()).isEqualTo(content);
  }
}
