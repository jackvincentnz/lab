package lab.libs.ddd.domain;

public class EventReductionException extends RuntimeException {

  public EventReductionException(String message, Throwable cause) {
    super(message, cause);
  }
}
