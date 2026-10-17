package lab.task.adapter.messaging;

import com.google.protobuf.Message;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lab.libs.ddd.domain.DomainEvent;
import lab.libs.messaging.CloudEventHeaders;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DomainEventPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(DomainEventPublisher.class);

  protected static final String TASK_TOPIC = "task";

  protected static final String EVENT_SOURCE = "/organizer/task";

  private final KafkaTemplate<String, Message> kafkaTemplate;

  private final EventMapperFactory eventMapperFactory;

  private final Supplier<UUID> eventIdSupplier;

  @Autowired
  public DomainEventPublisher(
      KafkaTemplate<String, Message> kafkaTemplate, EventMapperFactory eventMapperFactory) {
    this(kafkaTemplate, eventMapperFactory, UUID::randomUUID);
  }

  DomainEventPublisher(
      KafkaTemplate<String, Message> kafkaTemplate,
      EventMapperFactory eventMapperFactory,
      Supplier<UUID> eventIdSupplier) {
    this.kafkaTemplate = kafkaTemplate;
    this.eventMapperFactory = eventMapperFactory;
    this.eventIdSupplier = eventIdSupplier;
    initializeProducer();
  }

  private void initializeProducer() {
    // Reduce first send latency by eagerly fetching topic metadata
    kafkaTemplate.partitionsFor(TASK_TOPIC);
  }

  public void publish(List<DomainEvent> appliedEvents) {
    appliedEvents.stream().map(this::map).map(this::toRecord).forEach(this::sendRecord);
  }

  private Message map(DomainEvent domainEvent) {
    var mapper = eventMapperFactory.mapperFor(domainEvent);
    return mapper.map(domainEvent);
  }

  /*
   * This service has no outbox or event store to give an event a lasting id, so the id is minted
   * here. Kafka redelivers a record with the id it was sent with, but publishing the same event
   * again from this side mints a new id, which an outbox would fix.
   */
  private ProducerRecord<String, Message> toRecord(Message message) {
    var record = new ProducerRecord<String, Message>(TASK_TOPIC, message);
    CloudEventHeaders.set(
        record.headers(),
        eventIdSupplier.get(),
        message.getDescriptorForType().getFullName(),
        EVENT_SOURCE);
    return record;
  }

  private void sendRecord(ProducerRecord<String, Message> record) {
    var message = record.value();
    var future = kafkaTemplate.send(record);

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
