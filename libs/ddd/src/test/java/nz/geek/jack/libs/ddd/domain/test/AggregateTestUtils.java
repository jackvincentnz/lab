package nz.geek.jack.libs.ddd.domain.test;

import nz.geek.jack.libs.ddd.domain.Aggregate;

public class AggregateTestUtils {

  public static <T> T getLastEvent(Aggregate<?> aggregate, Class<T> clazz) {
    var events = aggregate.domainEvents();

    return clazz.cast(events.toArray()[events.size() - 1]);
  }

  public static int countEventsOfType(Aggregate<?> aggregate, Class<?> clazz) {
    return (int) aggregate.domainEvents().stream().filter(clazz::isInstance).count();
  }
}
