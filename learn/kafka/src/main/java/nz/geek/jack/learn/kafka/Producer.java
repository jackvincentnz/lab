package nz.geek.jack.learn.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

  protected static final String TOPIC = "test.topic";

  private final KafkaTemplate<String, SimpleMessage> kafkaTemplate;

  public Producer(KafkaTemplate<String, SimpleMessage> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String key, String value) {
    var message = SimpleMessage.newBuilder().setContent(value).build();

    kafkaTemplate
        .send(TOPIC, key, message)
        .thenAccept(
            (result) ->
                LOGGER.info(
                    String.format(
                        "Produced event to topic %s: key = %-10s value = %s", TOPIC, key, value)));
  }
}
