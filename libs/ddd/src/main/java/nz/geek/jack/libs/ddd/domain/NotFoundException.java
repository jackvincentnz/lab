package nz.geek.jack.libs.ddd.domain;

public class NotFoundException extends RuntimeException {
  public NotFoundException(AbstractId id) {
    super(String.format("%s [%s] not found", id.getClass().getSimpleName(), id));
  }
}
