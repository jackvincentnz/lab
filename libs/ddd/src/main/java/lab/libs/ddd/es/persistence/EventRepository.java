package lab.libs.ddd.es.persistence;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.libs.ddd.domain.DomainEvent;
import lab.libs.ddd.domain.InternalId;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class EventRepository {
  private static final String EVENT_ID_FIELD = "eventId";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
          .registerModule(new JavaTimeModule());

  public EventRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void appendEvents(UUID streamId, int expectedVersion, List<DomainEvent> events) {
    createStreamIfAbsent(streamId);

    var newVersion = calculateNewVersion(expectedVersion, events.size());
    validateAndUpdateStreamVersion(streamId, expectedVersion, newVersion);

    int latestVersion = expectedVersion;
    for (var event : events) {
      writeEvent(event, ++latestVersion);
    }
  }

  private void createStreamIfAbsent(UUID streamId) {
    jdbcTemplate.update(
        """
        INSERT INTO ES_STREAM (ID, VERSION)
        VALUES (:streamId, 0)
        ON CONFLICT DO NOTHING
        """,
        Map.of("streamId", streamId));
  }

  private int calculateNewVersion(int current, int eventCount) {
    return current + eventCount;
  }

  private void validateAndUpdateStreamVersion(UUID streamId, int expectedVersion, int newVersion) {
    if (!updateStreamVersion(streamId, expectedVersion, newVersion)) {
      throw new ConcurrentStreamWriteException(streamId, expectedVersion);
    }
  }

  private boolean updateStreamVersion(UUID streamId, int expectedVersion, int newVersion) {
    int updatedRows =
        jdbcTemplate.update(
            """
            UPDATE ES_STREAM
               SET VERSION = :newVersion
             WHERE ID = :streamId
               AND VERSION = :expectedVersion
            """,
            Map.of(
                "newVersion", newVersion,
                "streamId", streamId,
                "expectedVersion", expectedVersion));

    return updatedRows == 1;
  }

  private void writeEvent(DomainEvent<?> event, int version) {
    try {
      jdbcTemplate.update(
          """
          INSERT INTO ES_EVENT (TRANSACTION_ID, STREAM_ID, VERSION, EVENT_TYPE, JSON_DATA)
          VALUES(pg_current_xact_id(), :streamId, :version, :eventType, :jsonObj::json)
          """,
          Map.of(
              "streamId", event.getAggregateId().toUUID(),
              "version", version,
              "eventType", event.getClass().getName(),
              "jsonObj", objectMapper.writeValueAsString(event)));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public List<DomainEvent<?>> readEvents(UUID streamId) {
    return readEvents(streamId, null, null);
  }

  private List<DomainEvent<?>> readEvents(UUID streamId, Integer fromVersion, Integer toVersion) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("streamId", streamId);
    parameters.addValue("fromVersion", fromVersion, Types.INTEGER);
    parameters.addValue("toVersion", toVersion, Types.INTEGER);

    return jdbcTemplate.query(
        """
        SELECT ID,
               TRANSACTION_ID::text,
               STREAM_ID,
               VERSION,
               EVENT_TYPE,
               JSON_DATA
          FROM ES_EVENT
         WHERE STREAM_ID = :streamId
           AND (:fromVersion IS NULL OR VERSION > :fromVersion)
           AND (:toVersion IS NULL OR VERSION <= :toVersion)
         ORDER BY VERSION ASC
        """,
        parameters,
        this::toEvent);
  }

  private DomainEvent<InternalId> toEvent(ResultSet rs, int rowNum) {
    try {
      String eventType = rs.getString("EVENT_TYPE");
      PGobject jsonObj = (PGobject) rs.getObject("JSON_DATA");
      var json = (ObjectNode) objectMapper.readTree(jsonObj.getValue());
      if (!json.hasNonNull(EVENT_ID_FIELD)) {
        var legacyEventId =
            legacyEventId(rs.getObject("STREAM_ID", UUID.class), rs.getInt("VERSION"));
        json.put(EVENT_ID_FIELD, legacyEventId.toString());
      }
      Class<DomainEvent<InternalId>> clazz =
          (Class<DomainEvent<InternalId>>) Class.forName(eventType);
      return objectMapper.treeToValue(json, clazz);
    } catch (SQLException | ClassNotFoundException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * Events stored before events carried an id have none in their JSON. Their position in the
   * stream is unique and never changes, so an id derived from it stays the same on every read,
   * which a random id would not.
   */
  static UUID legacyEventId(UUID streamId, int version) {
    return UUID.nameUUIDFromBytes((streamId + ":" + version).getBytes(StandardCharsets.UTF_8));
  }
}
