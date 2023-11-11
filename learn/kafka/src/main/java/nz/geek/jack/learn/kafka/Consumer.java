package nz.geek.jack.learn.kafka;

import static nz.geek.jack.learn.kafka.Producer.TOPIC;

import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

  private CountDownLatch latch = new CountDownLatch(1);

  private String payload;

  @KafkaListener(topics = TOPIC, groupId = "spring-boot")
  public void listen(
      SimpleMessage message,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
    LOGGER.info(
        String.format(
            "Consumed event from topic %s: key = %-10s value = %s",
            topic, key, message.getContent()));

    payload = message.getContent();
    latch.countDown();
  }

  public CountDownLatch getLatch() {
    return latch;
  }

  public void resetLatch() {
    latch = new CountDownLatch(1);
  }

  public String getPayload() {
    return payload;
  }
}
