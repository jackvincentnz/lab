package nz.geek.jack.task.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.geek.jack.libs.ddd.domain.DomainEvent;
import nz.geek.jack.libs.ddd.domain.InternalId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherTest {

  @Mock KafkaTemplate<String, Message> kafkaTemplate;

  @Mock EventMapperFactory eventMapperFactory;

  @Captor ArgumentCaptor<Message> messageCaptor;

  @InjectMocks DomainEventPublisher domainEventPublisher;

  @Test
  void publish_mapsAndSendsAllEventsInOrder() {
    var domainEvents = mockEvents(true, false);

    when(eventMapperFactory.mapperFor(any(TestEvent.class))).thenReturn(new TestEventMapper());
    when(kafkaTemplate.send(any(), any()))
        .thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

    domainEventPublisher.publish(domainEvents);

    verify(kafkaTemplate, times(domainEvents.size()))
        .send(eq(DomainEventPublisher.TASK_TOPIC), messageCaptor.capture());
    var messages = messageCaptor.getAllValues();

    assertThat(messages.get(0)).isEqualTo(BoolValue.of(true));
    assertThat(messages.get(1)).isEqualTo(BoolValue.of(false));
  }

  List<DomainEvent> mockEvents(Boolean... events) {
    return Stream.of(events).map(TestEvent::new).collect(Collectors.toList());
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
