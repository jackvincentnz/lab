package lab.libs.ddd.domain;

import java.util.UUID;

public abstract class DomainEvent<A extends InternalId> {

  // Assigned once at creation so a consumer can tell a redelivery of this event from a new one.
  protected final UUID eventId;

  protected final A aggregateId;

  protected DomainEvent(A aggregateId) {
    this.eventId = UUID.randomUUID();
    this.aggregateId = aggregateId;
  }

  public UUID getEventId() {
    return eventId;
  }

  public A getAggregateId() {
    return aggregateId;
  }
}
