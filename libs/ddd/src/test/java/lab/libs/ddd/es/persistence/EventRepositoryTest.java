package lab.libs.ddd.es.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lab.libs.ddd.domain.DomainEvent;
import lab.libs.ddd.domain.InternalId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:tc:postgresql:latest:///databasename",
      "spring.sql.init.mode=always"
    })
class EventRepositoryTest {

  @Autowired EventRepository eventRepository;

  @Test
  void appendEvents_throwsWhenExpectedVersionDoesNotMatch() {
    var streamId = UUID.randomUUID();

    assertThrows(
        ConcurrentStreamWriteException.class,
        () -> eventRepository.appendEvents(streamId, 1, List.of()));
  }

  @Test
  void appendEvents_succeedsWhenExpectedVersionMatches() {
    var streamId = UUID.randomUUID();

    eventRepository.appendEvents(streamId, 0, List.of());
  }

  @Test
  void appendEvents_appendsEvents() {
    var streamId = UUID.randomUUID();

    var event = new TestEvent(streamId, "payload");

    eventRepository.appendEvents(streamId, 0, List.of(event));

    var readEvents = eventRepository.readEvents(streamId);

    assertThat(readEvents).hasSize(1);

    var readEvent = readEvents.get(0);
    assertThat(readEvent).isInstanceOf(TestEvent.class);

    var testEvent = (TestEvent) readEvent;
    assertThat(testEvent.getAggregateId().toUUID()).isEqualTo(streamId);
    assertThat(testEvent.value).isEqualTo(event.value);
  }

  @Test
  void appendEvents_supportsTime() {
    var streamId = new TestId();
    var time = Instant.now();
    var event = new TimeEvent(streamId, time);

    eventRepository.appendEvents(streamId.toUUID(), 0, List.of(event));

    var readEvents = eventRepository.readEvents(streamId.toUUID());

    var timeEvent = (TimeEvent) readEvents.get(0);
    assertThat(timeEvent.time).isEqualTo(time);
  }

  @SpringBootApplication
  static class EventRepositoryTestConfig {}

  static class TestEvent extends DomainEvent<TestId> {

    private final String value;

    protected TestEvent(UUID streamId, String value) {
      super(new TestId(streamId));
      this.value = value;
    }

    private TestEvent(
        @JsonProperty("aggregateId") TestId aggregateId, @JsonProperty("value") String value) {
      super(aggregateId);
      this.value = value;
    }
  }

  static class TestId extends InternalId {
    TestId(@JsonProperty("id") UUID streamId) {
      super(streamId);
    }

    TestId() {
      super();
    }
  }

  static class TimeEvent extends DomainEvent<TestId> {
    Instant time;

    TimeEvent(@JsonProperty("aggregateId") TestId streamId, @JsonProperty("time") Instant time) {
      super(streamId);
      this.time = time;
    }
  }
}
