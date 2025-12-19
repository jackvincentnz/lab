package lab.mops.core.domain.activityhierarchy;

public class DuplicateActivityTypeNameException extends RuntimeException {

  public DuplicateActivityTypeNameException() {
    super("Activity type names must be unique.");
  }
}
