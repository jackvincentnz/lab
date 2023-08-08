package nz.geek.jack.task.infrastructure.messaging;

import com.google.protobuf.Message;
import nz.geek.jack.libs.domain.DomainEvent;

public interface EventMapper<E extends DomainEvent, M extends Message> {

  M map(E domainEvent);
}
