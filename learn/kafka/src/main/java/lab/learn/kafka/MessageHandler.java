package lab.learn.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessageHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

  public void handle(SimpleMessage message, String topic, String key) {
    LOGGER.info(
        String.format(
            "Consumed event from topic %s: key = %-10s value = %s",
            topic, key, message.getContent()));
  }
}
