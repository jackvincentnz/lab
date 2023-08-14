package nz.geek.jack.task.adapter.messaging;

import com.google.protobuf.Message;
import java.util.List;
import nz.geek.jack.libs.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DomainEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(DomainEventPublisher.class);

  protected static final String TASK_TOPIC = "task";

  private final KafkaTemplate<String, Message> kafkaTemplate;

  private final EventMapperFactory eventMapperFactory;

  public DomainEventPublisher(
      KafkaTemplate<String, Message> kafkaTemplate, EventMapperFactory eventMapperFactory) {
    this.kafkaTemplate = kafkaTemplate;
    this.eventMapperFactory = eventMapperFactory;
  }

  public void publish(List<DomainEvent> appliedEvents) {
    appliedEvents.stream().map(this::map).forEach(this::sendMessage);
  }

  private Message map(DomainEvent domainEvent) {
    var mapper = eventMapperFactory.mapperFor(domainEvent);
    return mapper.map(domainEvent);
  }

  private void sendMessage(Message message) {
    var future = kafkaTemplate.send(TASK_TOPIC, message);

    future.whenComplete(
        (result, ex) -> {
          if (result != null) {
            var sentMsg =
                String.format(
                    "Produced event to topic %s: message = %s",
                    TASK_TOPIC, message.getDescriptorForType().getName());
            LOGGER.info(sentMsg);
          }

          if (ex != null) {
            var failedToSendMsg =
                String.format(
                    "Failed to send event to topic %s: message = %s",
                    TASK_TOPIC, message.getDescriptorForType().getName());
            throw new RuntimeException(failedToSendMsg, ex);
          }
        });
  }
}
