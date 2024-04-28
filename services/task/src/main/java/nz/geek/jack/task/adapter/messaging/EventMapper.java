package nz.geek.jack.task.adapter.messaging;

import com.google.protobuf.Message;
import nz.geek.jack.libs.ddd.domain.DomainEvent;

public interface EventMapper<E extends DomainEvent, M extends Message> {

  M map(E domainEvent);
}
