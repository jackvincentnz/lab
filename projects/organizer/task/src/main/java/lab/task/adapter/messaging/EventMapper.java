package lab.task.adapter.messaging;

import com.google.protobuf.Message;
import lab.libs.ddd.domain.DomainEvent;

public interface EventMapper<E extends DomainEvent, M extends Message> {

  M map(E domainEvent);
}
