package nz.geek.jack.libs.ddd.domain;

public class NotFoundException extends RuntimeException {
  public NotFoundException(InternalId id) {
    super(String.format("%s [%s] not found", id.getClass().getSimpleName(), id));
  }

  public NotFoundException(String name, String id) {
    super(String.format("%s [%s] not found", name, id));
  }
}
