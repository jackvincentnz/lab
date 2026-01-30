package lab.libs.ddd.domain;

public class NotFoundException extends RuntimeException {
  public NotFoundException(Object id) {
    super(String.format("%s [%s] not found", id.getClass().getSimpleName(), id));
  }
}
