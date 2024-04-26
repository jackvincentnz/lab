package nz.geek.jack.mops.plan.domain.activityhierarchy;

public class DuplicateActivityTypeNameException extends RuntimeException {

  public DuplicateActivityTypeNameException() {
    super("Activity type names must be unique.");
  }
}
