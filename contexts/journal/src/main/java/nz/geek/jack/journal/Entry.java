package nz.geek.jack.journal;

public class Entry {

  private final String message;
  private final String createdAt;

  public Entry(String message, String createdAt) {
    this.message = message;
    this.createdAt = createdAt;
  }

  public String getMessage() {
    return message;
  }

  public String getCreatedAt() {
    return createdAt;
  }
}
