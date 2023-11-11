package nz.geek.jack.learn.kafka;

import static nz.geek.jack.learn.kafka.Producer.TOPIC;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

  private final MessageHandler messageHandler;

  public Consumer(MessageHandler messageHandler) {
    this.messageHandler = messageHandler;
  }

  @KafkaListener(topics = TOPIC, groupId = "spring-boot")
  public void listen(
      SimpleMessage message,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
    messageHandler.handle(message, topic, key);
  }
}
