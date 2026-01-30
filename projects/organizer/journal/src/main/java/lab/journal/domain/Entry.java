package lab.journal.domain;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

public final class Entry {

  @Id private final EntryId id;

  private final String message;

  private final Instant createdAt;

  @Version private Integer version;

  private Entry(EntryId id, String message, Instant createdAt) {
    this.id = id;
    this.message = message;
    this.createdAt = createdAt;
  }

  public EntryId getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public static Entry newEntry(String message) {
    return new Entry(EntryId.create(), message, Instant.now());
  }
}
