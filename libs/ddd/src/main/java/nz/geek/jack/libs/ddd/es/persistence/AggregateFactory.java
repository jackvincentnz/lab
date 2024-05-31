package nz.geek.jack.libs.ddd.es.persistence;

import java.lang.reflect.InvocationTargetException;
import nz.geek.jack.libs.ddd.domain.Aggregate;
import org.springframework.stereotype.Component;

@Component
public class AggregateFactory {

  public <A extends Aggregate<?>> A forClass(Class<A> aggregateClass) {
    try {
      var constructor = aggregateClass.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new RuntimeException(
          String.format("Failed to instantiate instance of [%s]", aggregateClass.getSimpleName()),
          e);
    }
  }
}
