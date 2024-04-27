package nz.geek.jack.task.adapter.messaging;

import java.util.HashMap;
import java.util.Map;
import nz.geek.jack.libs.ddd.domain.DomainEvent;
import nz.geek.jack.task.domain.TaskAddedEvent;
import nz.geek.jack.task.domain.TaskCompletedEvent;
import org.springframework.stereotype.Component;

@Component
public class EventMapperFactory {

  private static final Map<Class<? extends DomainEvent>, EventMapper> EVENT_MAPPERS =
      new HashMap<>();

  static {
    EVENT_MAPPERS.put(TaskAddedEvent.class, new TaskAddedEventMapper());
    EVENT_MAPPERS.put(TaskCompletedEvent.class, new TaskCompletedEventMapper());
  }

  public EventMapper mapperFor(DomainEvent domainEvent) {
    var mapper = EVENT_MAPPERS.get(domainEvent.getClass());
    if (mapper == null) {
      throw new RuntimeException(
          String.format("No mapper found for: %s", domainEvent.getClass().getName()));
    }
    return mapper;
  }
}
