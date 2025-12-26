package nz.geek.jack.libs.ddd.domain;

public abstract class DomainEvent<A extends InternalId> {

  protected final A aggregateId;

  protected DomainEvent(A aggregateId) {
    this.aggregateId = aggregateId;
  }

  public A getAggregateId() {
    return aggregateId;
  }
}
