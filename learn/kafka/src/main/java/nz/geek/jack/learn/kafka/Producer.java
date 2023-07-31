package nz.geek.jack.learn.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {

  private static final Logger logger = LoggerFactory.getLogger(Producer.class);
  private static final String TOPIC = "purchases";

  private final KafkaTemplate<String, SimpleMessage> kafkaTemplate;

  public Producer(KafkaTemplate<String, SimpleMessage> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String key, String value) {
    var message = SimpleMessage.newBuilder().setContent(value).build();
    var future = kafkaTemplate.send(TOPIC, key, message);

    future.whenComplete(
        (result, ex) -> {
          if (result != null) {
            logger.info(
                String.format(
                    "Produced event to topic %s: key = %-10s value = %s", TOPIC, key, value));
          }

          if (ex != null) {
            ex.printStackTrace();
          }
        });
  }
}
