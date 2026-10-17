package lab.task.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lab.libs.ddd.domain.DomainEvent;
import lab.libs.ddd.domain.InternalId;
import lab.libs.messaging.CloudEventHeaders;
import lab.test.TestBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherTest extends TestBase {

  @Mock KafkaTemplate<String, Message> kafkaTemplate;

  @Mock EventMapperFactory eventMapperFactory;

  @Captor ArgumentCaptor<ProducerRecord<String, Message>> recordCaptor;

  List<UUID> eventIds;

  DomainEventPublisher domainEventPublisher;

  @BeforeEach
  void setUp() {
    eventIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    var nextEventIds = eventIds.iterator();
    domainEventPublisher =
        new DomainEventPublisher(kafkaTemplate, eventMapperFactory, nextEventIds::next);

    when(eventMapperFactory.mapperFor(any(TestEvent.class))).thenReturn(new TestEventMapper());
    when(kafkaTemplate.send(any(ProducerRecord.class)))
        .thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));
  }

  @Test
  void publish_mapsAndSendsAllEventsInOrder() {
    var domainEvents = mockEvents(true, false);

    domainEventPublisher.publish(domainEvents);

    verify(kafkaTemplate, times(domainEvents.size())).send(recordCaptor.capture());
    var records = recordCaptor.getAllValues();

    assertThat(records).allMatch(record -> record.topic().equals(DomainEventPublisher.TASK_TOPIC));
    assertThat(records.get(0).value()).isEqualTo(BoolValue.of(true));
    assertThat(records.get(1).value()).isEqualTo(BoolValue.of(false));
  }

  @Test
  void publish_events_setsAnEventIdOnEachRecord() {
    domainEventPublisher.publish(mockEvents(true, false));

    verify(kafkaTemplate, times(2)).send(recordCaptor.capture());
    var records = recordCaptor.getAllValues();

    assertThat(CloudEventHeaders.readId(records.get(0).headers())).contains(eventIds.get(0));
    assertThat(CloudEventHeaders.readId(records.get(1).headers())).contains(eventIds.get(1));
  }

  @Test
  void publish_event_setsTypeAndSourceHeaders() {
    domainEventPublisher.publish(mockEvents(true));

    verify(kafkaTemplate).send(recordCaptor.capture());
    var headers = recordCaptor.getValue().headers();

    assertThat(value(headers.lastHeader(CloudEventHeaders.TYPE).value()))
        .isEqualTo(BoolValue.getDescriptor().getFullName());
    assertThat(value(headers.lastHeader(CloudEventHeaders.SOURCE).value()))
        .isEqualTo(DomainEventPublisher.EVENT_SOURCE);
  }

  List<DomainEvent> mockEvents(Boolean... events) {
    return Stream.of(events).map(TestEvent::new).collect(Collectors.toList());
  }

  private static String value(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  static class TestEvent extends DomainEvent<InternalId> {
    private final boolean content;

    TestEvent(boolean content) {
      super(null);
      this.content = content;
    }
  }

  static class TestEventMapper implements EventMapper<TestEvent, BoolValue> {

    @Override
    public BoolValue map(TestEvent testEvent) {
      return BoolValue.newBuilder().setValue(testEvent.content).build();
    }
  }
}
