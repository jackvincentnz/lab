package nz.geek.jack.journal.infrastructure.gql;

public final class Entry {

  private final String id;
  private final String message;
  private final String createdAt;

  public Entry(String id, String message, String createdAt) {
    this.id = id;
    this.message = message;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  public String getCreatedAt() {
    return createdAt;
  }
}
