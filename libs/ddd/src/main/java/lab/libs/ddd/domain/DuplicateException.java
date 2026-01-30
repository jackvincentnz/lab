package lab.libs.ddd.domain;

public class DuplicateException extends RuntimeException {
  public DuplicateException(Class<?> clazz) {
    super(String.format("Attempted to add duplicate %s", clazz.getSimpleName()));
  }
}
