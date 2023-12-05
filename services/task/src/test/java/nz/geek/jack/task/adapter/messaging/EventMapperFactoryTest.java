package nz.geek.jack.task.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nz.geek.jack.libs.domain.DomainEvent;
import nz.geek.jack.task.domain.TaskAddedEvent;
import nz.geek.jack.task.domain.TaskCompletedEvent;
import org.junit.jupiter.api.Test;

class EventMapperFactoryTest {

  EventMapperFactory eventMapperFactory = new EventMapperFactory();

  @Test
  void mapperFor_throwsForUnknownEvent() {
    class UnknownEvent extends DomainEvent {}

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
