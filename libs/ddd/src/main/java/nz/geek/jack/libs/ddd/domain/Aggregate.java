package nz.geek.jack.libs.ddd.domain;

import java.util.Collection;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.DomainEvents;

public abstract class Aggregate<I extends AbstractId> extends AbstractAggregateRoot<Aggregate<I>> {

  @Id protected final I id;

  @Version protected int version;

  protected Aggregate(I id) {
    this.id = id;
  }

  public I getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  @DomainEvents
  @Override
  public Collection<Object> domainEvents() {
    return super.domainEvents();
  }
}
