package nz.geek.jack.libs.domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Aggregate {

  private static final String REDUCER_METHOD_NAME = "on";

  private static final Map<String, Method> REDUCER_METHODS = new HashMap<>();

  private final List<DomainEvent> appliedEvents = new ArrayList<>();

  protected void apply(DomainEvent domainEvent) {
    appliedEvents.add(domainEvent);
    reduce(domainEvent);
  }

  private void reduce(DomainEvent domainEvent) {
    var eventType = domainEvent.getClass();

    var reducer = getReducerFor(eventType);

    try {
      reducer.invoke(this, domainEvent);
    } catch (InvocationTargetException | IllegalAccessException e) {
      var reductionFailedMessage =
          String.format(
              "Failed to reduce event: [%s], in method: [void %s(%s event)]",
              eventType.getSimpleName(), REDUCER_METHOD_NAME, eventType.getSimpleName());

      if (e.getCause() != null) {
        throw new RuntimeException(reductionFailedMessage, e.getCause());
      }

      throw new RuntimeException(reductionFailedMessage, e);
    }
  }

  private Method getReducerFor(Class<? extends DomainEvent> eventType) {
    var thisType = this.getClass();

    var methodKey = String.format("%s:%s", thisType.getName(), eventType.getName());

    var reducer = REDUCER_METHODS.get(methodKey);
    if (reducer == null) {
      reducer = getAndCacheReducerFor(methodKey, eventType);
    }

    return reducer;
  }

  private Method getAndCacheReducerFor(String methodKey, Class<? extends DomainEvent> eventType) {
    synchronized (REDUCER_METHODS) {
      var method = findReducer(eventType);
      method.setAccessible(true);

      REDUCER_METHODS.put(methodKey, method);

      return method;
    }
  }

  private Method findReducer(Class<? extends DomainEvent> eventType) {
    try {
      return this.getClass().getDeclaredMethod(REDUCER_METHOD_NAME, eventType);
    } catch (Exception e) {
      var reducerNotFoundMessage =
          String.format(
              "Reducer method: [void %s(%s event)] missing. %s >>> %s",
              REDUCER_METHOD_NAME,
              eventType.getSimpleName(),
              e.getClass().getSimpleName(),
              e.getMessage());

      throw new IllegalArgumentException(reducerNotFoundMessage, e);
    }
  }

  public List<DomainEvent> flushEvents() {
    var flushedEvents = List.copyOf(appliedEvents);
    appliedEvents.clear();
    return flushedEvents;
  }
}
