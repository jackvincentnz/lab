package nz.geek.jack.autojournal.infrastructure.messaging;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class TaskEventConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskEventConsumer.class);

  private static final String LISTENER_ID = "autojournal";

  private static final String TASK_TOPIC = "task";

  @KafkaListener(id = LISTENER_ID, topics = TASK_TOPIC)
  public void listen(Message message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    LOGGER.info(
        String.format(
            "Consumed event from topic %s: value = %s", topic, message.getClass().getName()));
  }
}
