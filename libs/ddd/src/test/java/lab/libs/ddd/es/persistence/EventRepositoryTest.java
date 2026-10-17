package lab.libs.ddd.es.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.libs.ddd.domain.DomainEvent;
import lab.libs.ddd.domain.InternalId;
import lab.test.RequiresDocker;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = "spring.sql.init.mode=always")
@RequiresDocker
class EventRepositoryTest extends TestBase {

  static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer(
              DockerImageName.parse(
                      "postgres:18.6@sha256:74935e72241653ca55e0414067e6d8763aceb8a810eb51b452253ec3dcfc4336")
                  .withRepository("postgres"))
          .withDatabaseName("databasename");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired EventRepository eventRepository;

  @Autowired NamedParameterJdbcTemplate jdbcTemplate;

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

  @Test
  void readEvents_timeWithNanoseconds_keepsNanoseconds() {
    var streamId = new TestId();
    // Instant.now() has only microseconds on some platforms, so nanoseconds are set explicitly.
    var time = Instant.ofEpochSecond(Instant.now().getEpochSecond(), 123_456_789);
    var event = new TimeEvent(streamId, time);

    eventRepository.appendEvents(streamId.toUUID(), 0, List.of(event));

    var timeEvent = (TimeEvent) eventRepository.readEvents(streamId.toUUID()).get(0);
    assertThat(timeEvent.time).isEqualTo(time);
  }

  @Test
  void readEvents_appendedEvent_keepsEventId() {
    var streamId = UUID.randomUUID();
    var event = new TestEvent(streamId, randomString());

    eventRepository.appendEvents(streamId, 0, List.of(event));

    var readEvent = eventRepository.readEvents(streamId).get(0);
    assertThat(readEvent.getEventId()).isEqualTo(event.getEventId());
  }

  @Test
  void readEvents_storedWithoutEventId_returnsSameDerivedEventIdOnEveryRead() {
    var streamId = UUID.randomUUID();
    var value = randomString();
    insertWithoutEventId(streamId, value);

    var firstRead = eventRepository.readEvents(streamId).get(0);
    var secondRead = eventRepository.readEvents(streamId).get(0);

    assertThat(firstRead.getEventId()).isNotNull();
    assertThat(secondRead.getEventId()).isEqualTo(firstRead.getEventId());
    assertThat(((TestEvent) firstRead).value).isEqualTo(value);
  }

  @Test
  void readEvents_storedWithoutEventIdInDifferentStreams_returnsDistinctEventIds() {
    var firstStreamId = UUID.randomUUID();
    var secondStreamId = UUID.randomUUID();
    insertWithoutEventId(firstStreamId, randomString());
    insertWithoutEventId(secondStreamId, randomString());

    var firstEvent = eventRepository.readEvents(firstStreamId).get(0);
    var secondEvent = eventRepository.readEvents(secondStreamId).get(0);

    assertThat(firstEvent.getEventId()).isNotEqualTo(secondEvent.getEventId());
  }

  private void insertWithoutEventId(UUID streamId, String value) {
    eventRepository.appendEvents(streamId, 0, List.of());
    var json = String.format("{\"aggregateId\":{\"id\":\"%s\"},\"value\":\"%s\"}", streamId, value);
    jdbcTemplate.update(
        """
        INSERT INTO ES_EVENT (TRANSACTION_ID, STREAM_ID, VERSION, EVENT_TYPE, JSON_DATA)
        VALUES(pg_current_xact_id(), :streamId, 1, :eventType, :jsonObj::json)
        """,
        Map.of("streamId", streamId, "eventType", TestEvent.class.getName(), "jsonObj", json));
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
