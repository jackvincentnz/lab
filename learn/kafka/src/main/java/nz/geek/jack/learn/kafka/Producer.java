package nz.geek.jack.learn.kafka;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class Producer {

  private static final Logger logger = LoggerFactory.getLogger(Producer.class);
  private static final String TOPIC = "purchases";

  private final KafkaTemplate<String, String> kafkaTemplate;

  public Producer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String key, String value) {
    CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, key, value);

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
