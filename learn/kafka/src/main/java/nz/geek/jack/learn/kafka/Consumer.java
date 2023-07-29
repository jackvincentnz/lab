package nz.geek.jack.learn.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

  private final Logger logger = LoggerFactory.getLogger(Consumer.class);

  @KafkaListener(
      id = "myConsumer",
      topics = "purchases",
      groupId = "spring-boot",
      autoStartup = "false")
  public void listen(
      String value,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
    logger.info(
        String.format("Consumed event from topic %s: key = %-10s value = %s", topic, key, value));
  }
}
