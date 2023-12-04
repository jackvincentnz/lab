package nz.geek.jack.learn.testcontainers.eventstore;

import static org.assertj.core.api.Assertions.assertThat;

import com.eventstore.dbclient.AppendToStreamOptions;
import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.eventstore.dbclient.ExpectedRevision;
import com.eventstore.dbclient.ReadStreamOptions;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Based on: <a href="https://developers.eventstore.com/clients/grpc/#creating-a-client">Connecting
 * to EventStoreDb</a>
 */
@Testcontainers
public class EventStoreTest {
  @Container
  public GenericContainer eventStore =
      new GenericContainer(DockerImageName.parse("eventstore/eventstore:23.10.0-jammy"))
          .withCommand("--insecure", "--run-projections=All", "--enable-atom-pub-over-http")
          .withExposedPorts(2113);

  @Test
  void testEventStorePersistence() throws IOException, ExecutionException, InterruptedException {
    var connectionString =
        String.format(
            "esdb://%s:%s?tls=false&keepAliveTimeout=10000&keepAliveInterval=10000",
            eventStore.getHost(), eventStore.getFirstMappedPort());

    var connectionSettings = EventStoreDBConnectionString.parseOrThrow(connectionString);
    var client = EventStoreDBClient.create(connectionSettings);

    var createdEvent = new AccountCreated(UUID.randomUUID(), "ouros");
    var event = EventData.builderAsJson("account-created", createdEvent).build();

    var appendOptions = AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream());
    var appendResult = client.appendToStream("accounts", appendOptions, event).get();

    var readOptions = ReadStreamOptions.get().fromStart().notResolveLinkTos();
    var readResult = client.readStream("accounts", readOptions).get();

    var resolvedEvent = readResult.getEvents().get(0);
    var writtenEvent = resolvedEvent.getOriginalEvent().getEventDataAs(AccountCreated.class);

    assertThat(writtenEvent).isEqualTo(createdEvent);
  }
}
