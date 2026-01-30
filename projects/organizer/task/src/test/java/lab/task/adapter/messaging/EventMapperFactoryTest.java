package lab.task.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lab.libs.ddd.domain.DomainEvent;
import lab.libs.ddd.domain.InternalId;
import lab.task.domain.TaskAddedEvent;
import lab.task.domain.TaskCompletedEvent;
import org.junit.jupiter.api.Test;

class EventMapperFactoryTest {

  EventMapperFactory eventMapperFactory = new EventMapperFactory();

  @Test
  void mapperFor_throwsForUnknownEvent() {
    class UnknownEvent extends DomainEvent<InternalId> {
      UnknownEvent() {
        super(null);
      }
    }

    assertThrows(RuntimeException.class, () -> eventMapperFactory.mapperFor(new UnknownEvent()));
  }

  @Test
  void mapperFor_hasTaskAddedEventMapper() {
    var mapper = eventMapperFactory.mapperFor(TaskAddedEvent.of(null, null, null));

    assertThat(mapper).isNotNull();
  }

  @Test
  void mapperFor_hasTaskCompletedEventMapper() {
    var mapper = eventMapperFactory.mapperFor(TaskCompletedEvent.of(null));

    assertThat(mapper).isNotNull();
  }
}
