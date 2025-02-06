package nz.geek.jack.libs.ddd.domain;

public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}
