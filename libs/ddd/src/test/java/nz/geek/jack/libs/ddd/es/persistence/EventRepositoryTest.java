package nz.geek.jack.libs.ddd.es.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;
import nz.geek.jack.libs.ddd.domain.DomainEvent;
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

  static class TestId extends AbstractId {
    TestId(@JsonProperty("id") UUID streamId) {
      super(streamId);
    }
  }
}
