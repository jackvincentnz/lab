package lab.libs.ddd.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.DomainEvents;

public abstract class Aggregate<I extends InternalId> extends AbstractAggregateRoot<Aggregate<I>> {

  @Id protected final I id;

  @Version protected int version;

  protected Instant createdAt;

  protected Instant updatedAt;

  @Transient private Clock clock;

  protected Aggregate(I id) {
    this(id, Clock.systemDefaultZone());
  }

  protected Aggregate(I id, Clock clock) {
    Objects.requireNonNull(id, "id must not be null");
    this.id = id;
    this.clock = clock;
    this.createdAt = Instant.now(clock);
    this.updatedAt = createdAt;
  }

  public I getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  @DomainEvents
  @Override
  public Collection<Object> domainEvents() {
    return super.domainEvents();
  }

  @Override
  protected <T> T registerEvent(T event) {
    this.updatedAt = Instant.now(clock);
    return super.registerEvent(event);
  }

  void setClock(Clock clock) {
    this.clock = clock;
  }
}
