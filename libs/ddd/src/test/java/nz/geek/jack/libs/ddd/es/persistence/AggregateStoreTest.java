package nz.geek.jack.libs.ddd.es.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.libs.ddd.domain.DomainEvent;
import nz.geek.jack.libs.ddd.domain.EventSourcedAggregate;
import nz.geek.jack.libs.ddd.domain.InternalId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateStoreTest {

  @Mock EventRepository eventRepository;

  @Mock AggregateFactory aggregateFactory;

  @InjectMocks AggregateStore aggregateStore;

  @Captor ArgumentCaptor<List<DomainEvent>> eventsCaptor;

  @Test
  void save_flushesTheAggregate() {
    var aggregate = TestAggregate.create();

    aggregateStore.save(aggregate);

    assertThat(aggregate.flushEvents().size()).isEqualTo(0);
  }

  @Test
  void save_appendsChangesToEventStreamAtAggregateVersion() {
    var aggregate = TestAggregate.create();

    aggregateStore.save(aggregate);

    verify(eventRepository)
        .appendEvents(
            eq(aggregate.getId().toUUID()), eq(aggregate.getVersion()), eventsCaptor.capture());

    var events = eventsCaptor.getValue();
    assertThat(events.size()).isEqualTo(1);
  }

  @Test
  void get_hydratesAggregatesFromTheStore() {
    var id = new TestId();

    when(eventRepository.readEvents(id.toUUID()))
        .thenReturn(List.of(new CreateEvent(id), new ChangeEvent(id)));
    when(aggregateFactory.forClass(TestAggregate.class)).thenReturn(new TestAggregate());

    var result = aggregateStore.get(id, TestAggregate.class);

    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.count).isEqualTo(1);
  }

  static class TestAggregate extends EventSourcedAggregate<TestId> {

    int count = 0;

    static TestAggregate create() {
      var aggregate = new TestAggregate();
      aggregate.apply(new CreateEvent(new TestId()));
      return aggregate;
    }

    void on(CreateEvent event) {
      this.id = event.getAggregateId();
    }

    void on(ChangeEvent event) {
      this.count++;
    }
  }

  static class CreateEvent extends DomainEvent<TestId> {
    CreateEvent(TestId id) {
      super(id);
    }
  }

  static class ChangeEvent extends DomainEvent<TestId> {
    ChangeEvent(TestId id) {
      super(id);
    }
  }

  static class TestId extends InternalId {}
}
